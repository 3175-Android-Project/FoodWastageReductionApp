package com.project.foodwastagereductionapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.project.foodwastagereductionapp.profile.ProfilePhotoStore;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccountDataTest {
    private static final String DB = "account-security-test.db";
    private Context context;
    private AppDatabaseHelper db;
    private long first, second;
    @Before public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(DB);
        db = new AppDatabaseHelper(context, DB);
        String hash = PasswordHasher.hash("Original123");
        first = db.createUser("first@example.com", hash, User.CUSTOMER, "First", "Vancouver", "BC", "V6B 1A1", "405 Main St", "");
        second = db.createUser("second@example.com", hash, User.MANAGER, "Second", "Burnaby", "BC", "V5A 1S6", "100 Test St", "");
        db.confirmEmailLocally(first); db.confirmEmailLocally(second);
    }
    @After public void cleanup() { db.close(); context.deleteDatabase(DB); }
    private String grant(long userId, long issued, long expires) {
        String token = ResetTokens.create();
        PasswordResetGrantEntity grant = new PasswordResetGrantEntity();
        grant.userId = userId; grant.tokenHash = ResetTokens.hash(token);
        grant.issuedAt = issued; grant.expiresAt = expires;
        AppDatabase database = AppDatabase.getInstance(context, DB);
        database.passwordResetGrantDao().deleteForUser(userId);
        database.passwordResetGrantDao().insert(grant);
        return token;
    }
    private String grant(long userId) { long now = System.currentTimeMillis(); return grant(userId, now, now + 300000); }

    @Test public void resetRequiresBoundUnexpiredSingleUseGrant() {
        String token = grant(first);
        assertFalse(db.resetPassword("second@example.com", token, "Changed123"));
        assertFalse(db.resetPassword("first@example.com", "email-only", "Changed123"));
        assertFalse(db.resetPassword("first@example.com", null, "Changed123"));
        assertTrue(db.resetPassword(" FIRST@EXAMPLE.COM ", token, "Changed123"));
        assertFalse(db.resetPassword("first@example.com", token, "ChangedAgain123"));
        assertNull(db.authenticate("first@example.com", "Original123"));
        assertNotNull(db.authenticate("first@example.com", "Changed123"));
        assertNotNull(db.authenticate("second@example.com", "Original123"));
    }
    @Test public void expiredAndFutureGrantsDoNotModifyPassword() {
        long now = System.currentTimeMillis();
        assertFalse(db.resetPassword("first@example.com", grant(first, now - 600000, now - 1), "Changed123"));
        assertFalse(db.resetPassword("first@example.com", grant(first, now + 600000, now + 900000), "Changed123"));
        assertNotNull(db.authenticate("first@example.com", "Original123"));
    }
    @Test public void resetGrantSurvivesReopenAndIsConsumedAtomically() throws Exception {
        String token = grant(first);
        db.close(); db = new AppDatabaseHelper(context, DB);
        assertTrue(db.isPasswordResetGrantValid("first@example.com", token));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results = pool.invokeAll(Arrays.asList(
                    () -> db.resetPassword("first@example.com", token, "Concurrent123"),
                    () -> db.resetPassword("first@example.com", token, "Concurrent456")));
            int success = 0; for (Future<Boolean> result : results) if (result.get()) success++;
            assertEquals(1, success);
        } finally { pool.shutdownNow(); }
        assertFalse(db.isPasswordResetGrantValid("first@example.com", token));
    }
    @Test public void changePasswordRequiresCurrentPasswordAndInvalidatesResetGrants() {
        String token = grant(second);
        assertFalse(db.changePassword(second, "wrong", "Changed123"));
        assertTrue(db.isPasswordResetGrantValid("second@example.com", token));
        assertTrue(db.changePassword(second, "Original123", "  Changed123  "));
        assertFalse(db.isPasswordResetGrantValid("second@example.com", token));
        assertNotNull(db.authenticate("second@example.com", "  Changed123  "));
        assertNull(db.authenticate("second@example.com", "Changed123"));
    }
    @Test public void shortNewPasswordLeavesGrantAndPasswordIntact() {
        String token = grant(first);
        try { db.resetPassword("first@example.com", token, "short"); fail(); }
        catch (IllegalArgumentException expected) { }
        assertTrue(db.isPasswordResetGrantValid("first@example.com", token));
        assertNotNull(db.authenticate("first@example.com", "Original123"));
    }
    @Test public void photoAndFullAddressPersistAndAreIsolated() throws Exception {
        File source = new File(context.getCacheDir(), "account-test-photo.png");
        Bitmap image = Bitmap.createBitmap(2400, 800, Bitmap.Config.ARGB_8888);
        try (OutputStream output = new FileOutputStream(source)) { image.compress(Bitmap.CompressFormat.PNG, 100, output); }
        image.recycle();
        String uri = ProfilePhotoStore.importPhoto(context, Uri.fromFile(source));
        try {
            Bitmap saved = android.graphics.BitmapFactory.decodeFile(Uri.parse(uri).getPath());
            assertTrue(Math.max(saved.getWidth(), saved.getHeight()) <= 512); saved.recycle();
            db.updateProfile(first, "Updated", "123 Main Street", "Vancouver", "BC", "V6B 1A1", "6045550100", uri);
            db.close(); db = new AppDatabaseHelper(context, DB);
            User user = db.getUser(first);
            assertEquals(uri, user.profilePhotoUri);
            assertEquals("123 Main Street, Vancouver, BC V6B 1A1", user.getAddressLine());
            assertEquals("", db.getUser(second).profilePhotoUri);
            db.updateProfile(first, "Updated", "123 Main Street", "Vancouver", "BC", "V6B 1A1", "", "");
            assertEquals("", db.getUser(first).profilePhotoUri);
        } finally { new File(Uri.parse(uri).getPath()).delete(); source.delete(); }
    }
    @Test public void malformedPhotoIsRejected() throws Exception {
        File source = new File(context.getCacheDir(), "not-a-photo.txt");
        try (FileWriter output = new FileWriter(source)) { output.write("not an image"); }
        try { ProfilePhotoStore.importPhoto(context, Uri.fromFile(source)); fail(); }
        catch (IOException expected) { } finally { source.delete(); }
    }
    @Test public void migrationFromVersionFivePreservesAccountsOrdersAndFood() throws Exception {
        db.close(); context.deleteDatabase(DB);
        SQLiteDatabase legacy = context.openOrCreateDatabase(DB, 0, null);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("schema-v5.sql")))) {
            String line; while ((line = reader.readLine()) != null) legacy.execSQL(line);
        }
        legacy.execSQL("INSERT INTO users(id,email,password_hash,role,full_name,city,province,postal_code,email_verified,address,phone) "
                + "VALUES(1,'legacy@example.com',?,'customer','Legacy','Vancouver','BC','V6B 1A1',1,'405 Main St','6045550100')",
                new Object[]{PasswordHasher.hash("Legacy123")});
        legacy.execSQL("INSERT INTO users(id,email,role,email_verified) VALUES(2,'manager@example.com','manager',1)");
        legacy.execSQL("INSERT INTO restaurants(id,manager_id,name) VALUES(1,2,'Legacy Kitchen')");
        legacy.execSQL("INSERT INTO food_items(id,restaurantId,name,originalPrice,discountedPrice,quantityLeft,available) VALUES(1,1,'Legacy Food',10,5,2,1)");
        legacy.execSQL("INSERT INTO orders(id,customer_id,restaurant_id,restaurant_name,placed_at,item_count,total,status,is_delivery) VALUES(1,1,1,'Legacy Kitchen',1000,1,5.25,'Pending',0)");
        legacy.execSQL("INSERT INTO order_items(id,order_id,food_item_id,name,unit_price,quantity) VALUES(1,1,1,'Legacy Food',5,1)");
        legacy.execSQL("INSERT INTO notifications(id,customer_id,order_id,status,created_at) VALUES(1,1,1,'Ready',1000)");
        legacy.setVersion(5); legacy.close();
        db = new AppDatabaseHelper(context, DB);
        assertNotNull(db.authenticate("legacy@example.com", "Legacy123"));
        assertEquals("405 Main St", db.getUser(1).address);
        assertEquals("", db.getUser(1).profilePhotoUri);
        assertEquals(1, db.getOrders(1).size());
        assertEquals(1, db.getFoodItemsForRestaurant(1).size());
        assertEquals(1, db.getNotifications(1).size());
        assertEquals(1, db.getOrderDetails(1, 1).lines.size());
        assertTrue(db.isPasswordResetGrantValid("legacy@example.com", grant(1)));
    }
}
