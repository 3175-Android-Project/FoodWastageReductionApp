package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.RestaurantListing;
import com.project.foodwastagereductionapp.data.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class CustomerHomeActivity extends AppCompatActivity {

    private static final int ENDING_SOON_LIMIT = 3;

    private TextView txtLocation, txtNearbyTitle, txtNoNearby, txtEndingSoonTitle;
    private LinearLayout llNearby, llEndingSoon;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerViewContainer;
    private View scrollContent;
    private static List<RestaurantListing> cachedHomeRestaurants = null;
    private static User cachedHomeUser = null;

    private static Map<Long, List<FoodItem>> cachedHomeFood = null;

    public static void invalidateCache() {
        cachedHomeRestaurants = null;
        cachedHomeUser = null;
        cachedHomeFood = null;
    }

    private static final class HomeData {
        final User user;
        final List<RestaurantListing> restaurants;
        final Map<Long, List<FoodItem>> foodByRestaurant; // restaurant id -> its food (food_items)
        HomeData(User user, List<RestaurantListing> restaurants, Map<Long, List<FoodItem>> foodByRestaurant) {
            this.user = user;
            this.restaurants = restaurants;
            this.foodByRestaurant = foodByRestaurant;
        }
    }

    // Food loaded with the restaurants on the worker thread (see loadHome)
    private Map<Long, List<FoodItem>> foodByRestaurant = new HashMap<>();

    // A food item plus the restaurant it belongs to (for the Ending soon list).
    private static final class EndingSoonItem {
        final Restaurant restaurant;
        final FoodItem food;
        final Date until;

        EndingSoonItem(Restaurant restaurant, FoodItem food, Date until) {
            this.restaurant = restaurant;
            this.food = food;
            this.until = until;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtLocation = findViewById(R.id.txtLocation);
        txtNearbyTitle = findViewById(R.id.txtNearbyTitle);
        txtNoNearby = findViewById(R.id.txtNoNearby);
        txtEndingSoonTitle = findViewById(R.id.txtEndingSoonTitle);
        llNearby = findViewById(R.id.llNearby);
        llEndingSoon = findViewById(R.id.llEndingSoon);
        shimmerViewContainer = findViewById(R.id.shimmerViewContainer);
        scrollContent = findViewById(R.id.scrollContent);

        // Browse: search box and "See all" open the Browse screen
        findViewById(R.id.editSearch).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerBrowseActivity.class)));
        findViewById(R.id.txtSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerBrowseActivity.class)));

        // Bell opens the order notifications
        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // already here
            } else if (id == R.id.nav_search) {
                goTo(CustomerBrowseActivity.class);
                return true;
            } else if (id == R.id.nav_orders) {
                goTo(CustomerOrderActivity.class);
                return true;
            } else if (id == R.id.nav_profile) {
                goTo(CustomerProfileActivity.class);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHome(); // refresh (e.g. after editing the profile city)
    }

    // Database work on a worker thread (AppDatabaseHelper rule), UI updates on the main thread
    private void loadHome() {
        SessionManager session = new SessionManager(this);
        SessionManager.Snapshot snapshot = session.snapshot();
        long userId = snapshot.userId;
        if (cachedHomeUser != null && cachedHomeUser.id != userId) invalidateCache();
        boolean isColdLoad = (cachedHomeRestaurants == null || cachedHomeUser == null || cachedHomeFood == null);
        if (isColdLoad) {
            shimmerViewContainer.setVisibility(View.VISIBLE);
            shimmerViewContainer.startShimmer();
            scrollContent.setVisibility(View.GONE);
        } else {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            scrollContent.setVisibility(View.VISIBLE);
            foodByRestaurant = cachedHomeFood;
            bindHome(cachedHomeUser, cachedHomeRestaurants);
        }

        long startTime = System.currentTimeMillis();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        com.project.foodwastagereductionapp.auth.AuthTasks.run(this, null, () -> {
            User user = db.getUser(userId);
            List<RestaurantListing> all = db.searchRestaurants("");
            Map<Long, List<FoodItem>> food = new HashMap<>();
            for (RestaurantListing l : all) {
                food.put(l.restaurant.id, db.getFoodForRestaurant(l.restaurant.id));
            }
            return new HomeData(user, all, food);
        }, result -> {
            User user = result.user;
            List<RestaurantListing> all = result.restaurants;
            if (user == null || !user.emailVerified || !User.CUSTOMER.equals(user.role)) {
                com.project.foodwastagereductionapp.auth.AuthNavigator.logout(this);
                return;
            }
            cachedHomeUser = user;
            cachedHomeRestaurants = all;
            cachedHomeFood = result.foodByRestaurant;
            long elapsed = System.currentTimeMillis() - startTime;
            long delay = isColdLoad ? Math.max(0, 350 - elapsed) : 0;
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (isFinishing() || isDestroyed() || !session.isCurrent(snapshot)) return;
                shimmerViewContainer.stopShimmer();
                shimmerViewContainer.setVisibility(View.GONE);
                scrollContent.setVisibility(View.VISIBLE);
                foodByRestaurant = result.foodByRestaurant;
                bindHome(user, all);
            }, delay);
        });
    }

    private void bindHome(User user, List<RestaurantListing> all) {
        String city = user != null ? user.city : null;
        txtLocation.setText(city != null ? city : "");

        // Nearby: restaurants in the customer's city; if none, show all restaurants
        List<Restaurant> nearby = new ArrayList<>();
        if (city != null) {
            for (RestaurantListing l : all) {
                if (city.trim().equalsIgnoreCase(l.restaurant.city.trim())) nearby.add(l.restaurant);
            }
        }
        if (!nearby.isEmpty()) {
            txtNearbyTitle.setText(getString(R.string.fmtNearbyIn, city));
        } else {
            for (RestaurantListing l : all) nearby.add(l.restaurant);
            txtNearbyTitle.setText(R.string.titleAllRestaurants);
        }

        bindNearby(nearby);
        bindEndingSoon(nearby);
    }

    private void bindNearby(List<Restaurant> restaurants) {
        llNearby.removeAllViews();
        txtNoNearby.setVisibility(restaurants.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Restaurant r : restaurants) {
            View card = inflater.inflate(R.layout.item_home_restaurant, llNearby, false);

            setImage(card.findViewById(R.id.imgRestaurant), r.imageUri);
            ((TextView) card.findViewById(R.id.txtRestaurantName)).setText(r.name);
            ((TextView) card.findViewById(R.id.txtRestaurantAddress))
                    .setText(getString(R.string.fmtCityProvince, r.city, r.province));

            // Lowest discounted price and total items left (food_items table)
            TextView txtPrice = card.findViewById(R.id.txtPrice);
            TextView txtItemsLeft = card.findViewById(R.id.txtItemsLeft);
            double lowest = -1;
            int totalLeft = 0;
            for (FoodItem f : foodFor(r.id)) {
                if (f.quantityLeft <= 0) continue;
                totalLeft += f.quantityLeft;
                if (lowest < 0 || f.price < lowest) lowest = f.price;
            }
            if (totalLeft > 0) {
                txtPrice.setText(PriceFormatter.format(lowest));
                txtItemsLeft.setText(getString(R.string.fmtQuantityLeft, totalLeft));
                txtItemsLeft.setVisibility(View.VISIBLE);
            } else {
                txtPrice.setText(r.cuisineType);
                txtItemsLeft.setVisibility(View.GONE);
            }

            card.setOnClickListener(v -> openRestaurant(r.id));
            llNearby.addView(card);
        }
    }

    // Top 3 food items (still available) with the soonest "until" time.
    private void bindEndingSoon(List<Restaurant> restaurants) {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        List<EndingSoonItem> items = new ArrayList<>();
        for (Restaurant r : restaurants) {
            for (FoodItem f : foodFor(r.id)) {
                if (f.quantityLeft <= 0 || f.availableUntil == null) continue;
                try {
                    items.add(new EndingSoonItem(r, f, format.parse(f.availableUntil)));
                } catch (ParseException ignored) {
                    // Unknown time format: skip it
                }
            }
        }
        Collections.sort(items, (a, b) -> a.until.compareTo(b.until));

        llEndingSoon.removeAllViews();
        boolean hasItems = !items.isEmpty();
        txtEndingSoonTitle.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        llEndingSoon.setVisibility(hasItems ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < Math.min(ENDING_SOON_LIMIT, items.size()); i++) {
            EndingSoonItem e = items.get(i);
            View card = inflater.inflate(R.layout.item_home_ending_soon, llEndingSoon, false);

            ImageView imgFood = card.findViewById(R.id.imgFood);
            if (e.food.hasImageUri()) {
                setImage(imgFood, e.food.imageUri);
            } else if (e.food.imageRes != 0) {
                imgFood.setImageResource(e.food.imageRes);
            }
            ((TextView) card.findViewById(R.id.txtFoodName)).setText(e.food.name);
            ((TextView) card.findViewById(R.id.txtRestaurantInfo))
                    .setText(getString(R.string.fmtRestaurantCity, e.restaurant.name, e.restaurant.city));
            ((TextView) card.findViewById(R.id.txtPriceUntil)).setText(getString(
                    R.string.fmtPriceUntil, PriceFormatter.format(e.food.price), e.food.availableUntil));

            card.setOnClickListener(v -> openFood(e.restaurant.id, e.food.id));
            llEndingSoon.addView(card);
        }
    }

    // Food for one restaurant, loaded in loadHome() (empty list if none)
    private List<FoodItem> foodFor(long restaurantId) {
        List<FoodItem> food = foodByRestaurant.get(restaurantId);
        return food != null ? food : new ArrayList<>();
    }

    private void setImage(ImageView view, String uri) {
        if (uri != null && !uri.isEmpty()) view.setImageURI(Uri.parse(uri));
    }

    private void openRestaurant(long restaurantId) {
        Intent intent = new Intent(this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }

    private void openFood(long restaurantId, int foodId) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra(FoodDetailActivity.EXTRA_RESTAURANT_ID, restaurantId);
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_ID, foodId);
        startActivity(intent);
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}
