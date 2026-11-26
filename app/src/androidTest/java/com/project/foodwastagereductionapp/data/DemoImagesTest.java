package com.project.foodwastagereductionapp.data;

import android.app.Activity;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.customer.*;
import com.project.foodwastagereductionapp.manager.*;
import com.project.foodwastagereductionapp.ui.LocalImageLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class DemoImagesTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test public void seedBackfillsOnlyBlankDemoPhotosAndSurvivesReopen() throws Exception {
        String name = "demo-photos-" + System.nanoTime() + ".db";
        AppDatabaseHelper helper = new AppDatabaseHelper(context, name);
        AppDatabase db = AppDatabase.getInstance(context, name);
        try {
            helper.seedDemoDataIfEmpty();
            Set<String> uris = new HashSet<>();
            int count = 0;
            for (DemoImages.RestaurantPhotos photos : DemoImages.RESTAURANTS) {
                RestaurantEntity r = db.restaurantDao().findByManagerId(db.userDao().findByEmail(photos.managerEmail).id);
                assertEquals(DemoImages.uri(context, photos.restaurantImage), r.imageUri);
                assertTrue(uris.add(r.imageUri));
                List<FoodItemEntity> foods = db.foodItemDao().getRestaurantFoodItems(r.id);
                assertEquals(photos.foodNames.length, foods.size());
                for (int i = 0; i < foods.size(); i++) {
                    FoodItemEntity f = foods.get(i);
                    assertEquals(photos.foodNames[i], f.name);
                    assertEquals(DemoImages.uri(context, photos.foodImages[i]), f.imageUri);
                    assertTrue(uris.add(f.imageUri));
                    try (InputStream stream = context.getContentResolver().openInputStream(Uri.parse(f.imageUri))) { assertNotNull(stream); }
                    count++;
                }
            }
            assertEquals(11, count); assertEquals(15, uris.size());
            db.getOpenHelper().getWritableDatabase().execSQL("UPDATE restaurants SET image_uri = NULL");
            db.getOpenHelper().getWritableDatabase().execSQL("UPDATE food_items SET imageUri = '  '");
            RestaurantEntity sushi = db.restaurantDao().findByManagerId(db.userDao().findByEmail("manager@grabbit.demo").id);
            FoodItemEntity salmon = db.foodItemDao().getRestaurantFoodItems(sushi.id).get(0);
            String upload = "content://presentation-test/user-selected-photo";
            db.restaurantDao().fillDemoImage(sushi.id, upload);
            salmon.imageUri = upload; salmon.quantityLeft = 2; db.foodItemDao().update(salmon);
            long realManager = helper.createUser("real@example.com", PasswordHasher.hash("123456"), User.MANAGER, "Real Manager", "Vancouver", "BC", "V6B 1A1");
            helper.confirmEmailLocally(realManager);
            helper.saveRestaurant(new Restaurant(0, realManager, "Sushi Barff", "Vancouver", "BC", "V6B 1A1", "604-555-0999", "Japanese", "", ""));
            long realRestaurant = helper.getRestaurantForManager(realManager).id;
            long realFood = helper.createFoodItem(realRestaurant, "Salmon oshi", "", "Japanese", 10, 5, 9, "11:50 PM", true, "");
            helper.seedDemoDataIfEmpty(); helper.seedDemoDataIfEmpty();
            assertEquals(upload, db.restaurantDao().findById(sushi.id).imageUri);
            assertEquals(upload, db.foodItemDao().getFoodItem(salmon.id).imageUri);
            assertEquals(2, db.foodItemDao().getFoodItem(salmon.id).quantityLeft);
            assertEquals("", db.restaurantDao().findById(realRestaurant).imageUri);
            assertEquals("", db.foodItemDao().getFoodItem(realFood).imageUri);
            assertEquals(5, db.restaurantDao().count());
            for (DemoImages.RestaurantPhotos photos : DemoImages.RESTAURANTS) {
                RestaurantEntity r = db.restaurantDao().findByManagerId(db.userDao().findByEmail(photos.managerEmail).id);
                assertFalse(r.imageUri.trim().isEmpty());
                for (FoodItemEntity f : db.foodItemDao().getRestaurantFoodItems(r.id)) assertFalse(f.imageUri.trim().isEmpty());
            }
            db.close(); db = AppDatabase.getInstance(context, name);
            assertEquals(upload, db.foodItemDao().getFoodItem(salmon.id).imageUri);
            assertTrue(db.foodItemDao().getRestaurantFoodItems(sushi.id).get(3).imageUri.endsWith("/img_sushi_takoyaki"));
        } finally { db.close(); context.deleteDatabase(name); }
    }

    @Test public void allPhotosDecodeWithinLimitAndUploadedFilesStillLoad() throws Exception {
        File upload = new File(context.getCacheDir(), "demo-image-upload.png");
        Bitmap source = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888); source.eraseColor(android.graphics.Color.MAGENTA);
        try (FileOutputStream out = new FileOutputStream(upload)) { source.compress(Bitmap.CompressFormat.PNG, 100, out); }
        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                ImageView view = new ImageView(context);
                for (DemoImages.RestaurantPhotos photos : DemoImages.RESTAURANTS) {
                    checkDecoded(view, DemoImages.uri(context, photos.restaurantImage));
                    for (int image : photos.foodImages) checkDecoded(view, DemoImages.uri(context, image));
                }
                LocalImageLoader.load(view, Uri.fromFile(upload).toString());
                assertTrue(source.sameAs(((BitmapDrawable) view.getDrawable()).getBitmap()));
                LocalImageLoader.load(view, "android.resource://" + context.getPackageName() + "/drawable/does_not_exist");
                assertNotNull(view.getDrawable()); assertFalse(view.getDrawable() instanceof BitmapDrawable);
            });
        } finally { upload.delete(); source.recycle(); }
    }
    private void checkDecoded(ImageView view, String uri) {
        LocalImageLoader.load(view, uri); assertTrue(uri, photo(view));
        Bitmap b = ((BitmapDrawable) view.getDrawable()).getBitmap();
        assertTrue(uri, b.getWidth() > 0 && b.getHeight() > 0 && Math.max(b.getWidth(), b.getHeight()) <= 1024);
    }

    @Test public void customerAndManagerScreensDisplayAllPhotos() throws Exception {
        assumeTrue("Use the isolated test APK", context.getPackageName().endsWith(".accounttest"));
        AppDatabaseHelper helper = new AppDatabaseHelper(context, AppDatabaseHelper.DATABASE_NAME);
        helper.seedDemoDataIfEmpty();
        AppDatabase db = AppDatabase.getInstance(context, AppDatabaseHelper.DATABASE_NAME);
        SessionManager session = new SessionManager(context);
        try {
            long customer = db.userDao().findByEmail("customer@grabbit.demo").id;
            session.start(customer, false);
            try (ActivityScenario<CustomerHomeActivity> screen = ActivityScenario.launch(CustomerHomeActivity.class)) {
                await(screen, a -> groupReady(a.findViewById(R.id.llNearby)) && groupReady(a.findViewById(R.id.llEndingSoon)));
                screen.onActivity(a -> { checkGroup(a.findViewById(R.id.llNearby)); checkGroup(a.findViewById(R.id.llEndingSoon)); });
                capture("customer-home.png");
            }
            try (ActivityScenario<CustomerBrowseActivity> screen = ActivityScenario.launch(CustomerBrowseActivity.class)) {
                await(screen, a -> ((RecyclerView) a.findViewById(R.id.recyclerRestaurants)).getAdapter().getItemCount() == 4);
                for (int i = 0; i < 4; i++) checkRow(screen, R.id.recyclerRestaurants, i, R.id.imgRestaurant, null);
                screen.onActivity(a -> ((RecyclerView) a.findViewById(R.id.recyclerRestaurants)).scrollToPosition(0));
                capture("customer-search.png");
            }
            for (DemoImages.RestaurantPhotos photos : DemoImages.RESTAURANTS) {
                long manager = db.userDao().findByEmail(photos.managerEmail).id;
                Restaurant restaurant = helper.getRestaurantForManager(manager);
                List<FoodItemEntity> foods = helper.getFoodItemsForRestaurant(restaurant.id);
                Intent detail = new Intent(context, RestaurantDetailActivity.class).putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.id);
                try (ActivityScenario<RestaurantDetailActivity> screen = ActivityScenario.launch(detail)) {
                    await(screen, a -> photo(a.findViewById(R.id.imgRestaurant)));
                    screen.onActivity(a -> assertPhoto(a.findViewById(R.id.imgRestaurant), restaurant.imageUri));
                    for (int i = 0; i < foods.size(); i++) checkRow(screen, R.id.recyclerFood, i, R.id.imgFood, foods.get(i).imageUri);
                    screen.onActivity(a -> ((RecyclerView) a.findViewById(R.id.recyclerFood)).scrollToPosition(0));
                    capture("restaurant-" + restaurant.id + ".png");
                }
                Intent foodDetail = new Intent(context, FoodDetailActivity.class).putExtra(FoodDetailActivity.EXTRA_RESTAURANT_ID, restaurant.id).putExtra(FoodDetailActivity.EXTRA_FOOD_ID, (int) foods.get(0).id);
                try (ActivityScenario<FoodDetailActivity> screen = ActivityScenario.launch(foodDetail)) {
                    await(screen, a -> photo(a.findViewById(R.id.imgFood)));
                    screen.onActivity(a -> assertPhoto(a.findViewById(R.id.imgFood), foods.get(0).imageUri));
                    capture("food-detail-" + restaurant.id + ".png");
                }
                session.start(manager, false); ManageFoodItemsActivity.invalidateCache();
                try (ActivityScenario<ManageFoodItemsActivity> screen = ActivityScenario.launch(ManageFoodItemsActivity.class)) {
                    await(screen, a -> ((RecyclerView) a.findViewById(R.id.recyclerFoods)).getAdapter().getItemCount() == foods.size());
                    for (int i = 0; i < foods.size(); i++) checkRow(screen, R.id.recyclerFoods, i, R.id.imgFood, foods.get(i).imageUri);
                    screen.onActivity(a -> assertNull(((ImageView) ((RecyclerView) a.findViewById(R.id.recyclerFoods)).getChildAt(0).findViewById(R.id.imgFood)).getImageTintList()));
                    capture("manager-items-" + restaurant.id + ".png");
                }
                try (ActivityScenario<AddFoodItemActivity> screen = ActivityScenario.launch(new Intent(context, AddFoodItemActivity.class).putExtra("foodId", foods.get(0).id))) {
                    await(screen, a -> photo(a.findViewById(R.id.imgFood)));
                    screen.onActivity(a -> assertPhoto(a.findViewById(R.id.imgFood), foods.get(0).imageUri));
                }
                try (ActivityScenario<ManagerProfileActivity> screen = ActivityScenario.launch(ManagerProfileActivity.class)) {
                    await(screen, a -> photo(a.findViewById(R.id.imgManagerProfile)));
                    screen.onActivity(a -> assertPhoto(a.findViewById(R.id.imgManagerProfile), restaurant.imageUri));
                }
                session.start(customer, false);
            }
        } finally { session.clear(); }
    }

    private boolean photo(ImageView image) { return image != null && image.getDrawable() instanceof BitmapDrawable; }
    private boolean groupReady(ViewGroup group) { return group.getChildCount() > 0 && photo(group.getChildAt(0).findViewById(R.id.imgFood)); }
    private void checkGroup(ViewGroup group) { for (int i = 0; i < group.getChildCount(); i++) assertTrue(photo(group.getChildAt(i).findViewById(R.id.imgFood))); }
    private void assertPhoto(ImageView actual, String uri) {
        ImageView expected = new ImageView(context); LocalImageLoader.load(expected, uri); assertTrue(uri, photo(actual));
        assertTrue(uri, ((BitmapDrawable) expected.getDrawable()).getBitmap().sameAs(((BitmapDrawable) actual.getDrawable()).getBitmap()));
    }
    private <A extends Activity> void checkRow(ActivityScenario<A> screen, int recycler, int position, int image, String uri) {
        screen.onActivity(a -> ((RecyclerView) a.findViewById(recycler)).scrollToPosition(position));
        await(screen, a -> { RecyclerView.ViewHolder h = ((RecyclerView) a.findViewById(recycler)).findViewHolderForAdapterPosition(position); return h != null && photo(h.itemView.findViewById(image)); });
        if (uri != null) screen.onActivity(a -> assertPhoto(((RecyclerView) a.findViewById(recycler)).findViewHolderForAdapterPosition(position).itemView.findViewById(image), uri));
    }
    private <A extends Activity> void await(ActivityScenario<A> screen, Predicate<A> ready) {
        long end = SystemClock.uptimeMillis() + 15000;
        while (SystemClock.uptimeMillis() < end) {
            AtomicBoolean result = new AtomicBoolean(); screen.onActivity(a -> result.set(ready.test(a)));
            if (result.get()) return; SystemClock.sleep(60);
        }
        fail("Timed out waiting for photos");
    }
    private void capture(String name) throws Exception {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("captureDemoPhotos"))) return;
        InstrumentationRegistry.getInstrumentation().waitForIdleSync(); SystemClock.sleep(200);
        File dir = new File(context.getFilesDir(), "demo-photo-screens"); dir.mkdirs();
        Bitmap image = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        try (FileOutputStream out = new FileOutputStream(new File(dir, name))) { image.compress(Bitmap.CompressFormat.PNG, 100, out); } finally { image.recycle(); }
    }
}
