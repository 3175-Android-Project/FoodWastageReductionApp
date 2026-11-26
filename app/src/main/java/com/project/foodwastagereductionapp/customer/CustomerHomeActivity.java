package com.project.foodwastagereductionapp.customer;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.RestaurantListing;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.ui.CityPickerBottomSheet;
import com.project.foodwastagereductionapp.ui.LocationHelper;
import android.graphics.Paint;

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
    private static final double NEARBY_RADIUS_KM = 35;

    // Where "Nearby" is measured from: the phone's location, or a city the customer picked.
    // Static so it survives the screen being recreated by the bottom navigation.
    private static boolean useCurrentLocation = true;
    private static Double originLat = null, originLng = null;
    private static String originLabel = null;
    private static boolean askedLocationPermission = false;
    private static boolean backfillStarted = false;

    // restaurant id -> km from the origin (empty when there is no origin)
    private final Map<Long, Double> distanceKm = new HashMap<>();

    private final ActivityResultLauncher<String[]> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (LocationHelper.hasPermission(this)) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(this, R.string.msgLocationUnavailable, Toast.LENGTH_SHORT).show();
                }
            });

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

        // Location: tap to pick a city or go back to the phone's location
        findViewById(R.id.llLocation).setOnClickListener(v -> showLocationPicker());
        if (useCurrentLocation) requestCurrentLocation(false);

        // Browse: search box and "See all" open the Browse screen
        findViewById(R.id.editSearch).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerBrowseActivity.class)));
        findViewById(R.id.txtSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerBrowseActivity.class)));

        // Bell opens the order notifications
        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // Cart opens the list of carts, one per restaurant (nothing to show when empty)
        findViewById(R.id.btnCart).setOnClickListener(v -> {
            if (CartManager.count() == 0) {
                Toast.makeText(this, R.string.msgCartEmpty, Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, CartsActivity.class));
            }
        });

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
        checkUnreadNotifications();
        updateCartBadge(); // the cart may have changed on another screen
    }

    /** Green number on the cart icon: how many items are in the cart (hidden when empty). */
    private void updateCartBadge() {
        TextView badge = findViewById(R.id.txtCartBadge);
        int count = CartManager.count();
        badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        badge.setText(count > 99 ? getString(R.string.fmtCartCountMax) : String.valueOf(count));
    }

    private void checkUnreadNotifications() {
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        com.project.foodwastagereductionapp.auth.AuthTasks.run(this, null, () -> {
            List<AppNotification> notifs = db.getNotifications(userId);
            if (notifs == null || notifs.isEmpty()) return false;
            long lastSeen = getSharedPreferences("grabbit_notifications_prefs", MODE_PRIVATE)
                    .getLong("last_seen_notif_" + userId, 0);
            for (AppNotification n : notifs) {
                if (n.timeMillis > lastSeen) return true;
            }
            return false;
        }, hasUnread -> {
            if (hasUnread != null) {
                findViewById(R.id.notificationBadge).setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            }
        });
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
            backfillRestaurantLocations(all);
        });
    }

    // ---- Location ---------------------------------------------------------------------------

    /** Ask for permission once (or again when the customer picks "current location"). */
    private void requestCurrentLocation(boolean userAsked) {
        if (LocationHelper.hasPermission(this)) {
            fetchCurrentLocation();
        } else if (userAsked || !askedLocationPermission) {
            askedLocationPermission = true;
            locationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void fetchCurrentLocation() {
        if (originLat == null) txtLocation.setText(R.string.txtLocating);
        LocationHelper.getCurrentLocation(this, location -> {
            if (!useCurrentLocation || isFinishing() || isDestroyed()) return; // picked a city meanwhile
            if (location == null) {
                if (originLat == null) {
                    Toast.makeText(this, R.string.msgLocationUnavailable, Toast.LENGTH_SHORT).show();
                    rebindHome();
                }
                return;
            }
            double lat = location.getLatitude(), lng = location.getLongitude();
            if (LocationHelper.isEmulatorMountainView(lat, lng)) {
                lat = 49.2057;
                lng = -122.9110;
            }
            double finalLat = lat;
            double finalLng = lng;
            // City name for the label (network lookup, so off the main thread)
            new Thread(() -> {
                String city = LocationHelper.cityFrom(this, finalLat, finalLng);
                if (city == null || city.equalsIgnoreCase("Mountain View") || city.equalsIgnoreCase("California")) {
                    city = "New Westminster";
                }
                String finalCity = city;
                runOnUiThread(() -> {
                    if (!useCurrentLocation || isFinishing() || isDestroyed()) return;
                    originLat = finalLat;
                    originLng = finalLng;
                    originLabel = finalCity;
                    rebindHome();
                });
            }).start();
        });
    }

    private void showLocationPicker() {
        String current = getString(R.string.optCurrentLocation);
        List<String> options = new ArrayList<>();
        options.add(current);
        options.addAll(CityPickerBottomSheet.cities());

        CityPickerBottomSheet.showOptions(this, R.string.titleSelectCity, options, choice -> {
            if (choice.equals(current)) {
                useCurrentLocation = true;
                requestCurrentLocation(true);
                return;
            }
            // A city: measure from its centre (falls back to a city-name match if offline)
            useCurrentLocation = false;
            originLat = null;
            originLng = null;
            originLabel = choice;
            rebindHome();
            new Thread(() -> {
                double[] latLng = LocationHelper.geocode(this, choice + ", BC, Canada");
                runOnUiThread(() -> {
                    if (latLng == null || useCurrentLocation || !choice.equals(originLabel)) return;
                    originLat = latLng[0];
                    originLng = latLng[1];
                    rebindHome();
                });
            }).start();
        });
    }

    /** Restaurants saved before the location update have no map position yet: look it up once. */
    private void backfillRestaurantLocations(List<RestaurantListing> all) {
        if (backfillStarted) return;
        List<Restaurant> missing = new ArrayList<>();
        for (RestaurantListing l : all) if (!l.restaurant.hasLocation()) missing.add(l.restaurant);
        if (missing.isEmpty()) return;
        backfillStarted = true;

        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        new Thread(() -> {
            boolean changed = false;
            for (Restaurant r : missing) {
                double[] latLng = LocationHelper.geocode(this,
                        r.postalCode + ", " + r.city + ", " + r.province + ", Canada");
                if (latLng != null) {
                    db.updateRestaurantLocation(r.id, latLng[0], latLng[1]);
                    changed = true;
                }
            }
            if (!changed) return;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                invalidateCache();
                loadHome();
            });
        }).start();
    }

    private void rebindHome() {
        if (cachedHomeUser != null && cachedHomeRestaurants != null) {
            bindHome(cachedHomeUser, cachedHomeRestaurants);
        }
    }

    private void bindHome(User user, List<RestaurantListing> all) {
        // Label: phone/picked location, else the profile city
        String city = originLabel != null ? originLabel : (user != null ? user.city : null);
        txtLocation.setText(city != null ? city : "");

        distanceKm.clear();
        if (originLat != null && originLng != null) {
            bindHomeByDistance(all, city);
            return;
        }

        // No location: restaurants in the customer's city; if none, show all restaurants
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

    // Nearby = within NEARBY_RADIUS_KM of the origin, closest first; if none, all restaurants by distance
    private void bindHomeByDistance(List<RestaurantListing> all, String city) {
        List<Restaurant> nearby = new ArrayList<>();
        List<Restaurant> everyone = new ArrayList<>();
        for (RestaurantListing l : all) {
            Restaurant r = l.restaurant;
            everyone.add(r);
            if (r.hasLocation()) {
                double km = LocationHelper.distanceKm(originLat, originLng, r.latitude, r.longitude);
                distanceKm.put(r.id, km);
                if (km <= NEARBY_RADIUS_KM) nearby.add(r);
            } else if (city != null && city.trim().equalsIgnoreCase(r.city.trim())) {
                nearby.add(r); // no map position yet: same city counts as nearby
            }
        }

        if (!nearby.isEmpty()) {
            txtNearbyTitle.setText(useCurrentLocation
                    ? getString(R.string.titleNearbyYou)
                    : getString(R.string.fmtNearbyIn, city));
        } else {
            nearby = everyone;
            txtNearbyTitle.setText(R.string.titleAllRestaurants);
        }
        Collections.sort(nearby, (a, b) -> Double.compare(distanceOf(a), distanceOf(b)));

        bindNearby(nearby);
        bindEndingSoon(nearby);
    }

    private double distanceOf(Restaurant r) {
        Double km = distanceKm.get(r.id);
        return km != null ? km : Double.MAX_VALUE; // unknown distance goes last
    }

    // Nearby: one card per food item still available, closest restaurant first
    private void bindNearby(List<Restaurant> restaurants) {
        llNearby.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int shown = 0;

        for (Restaurant r : restaurants) {
            Double km = distanceKm.get(r.id);
            String restaurantInfo = km != null
                    ? getString(R.string.fmtRestaurantDistance, r.name, km)
                    : getString(R.string.fmtRestaurantCity, r.name, r.city);

            for (FoodItem f : foodFor(r.id)) {
                if (f.quantityLeft <= 0) continue; // sold out
                View card = inflater.inflate(R.layout.item_home_nearby_food, llNearby, false);

                ImageView imgFood = card.findViewById(R.id.imgFood);
                if (f.hasImageUri()) {
                    setImage(imgFood, f.imageUri);
                } else if (f.imageRes != 0) {
                    imgFood.setImageResource(f.imageRes);
                }
                ((TextView) card.findViewById(R.id.txtFoodName)).setText(f.name);
                ((TextView) card.findViewById(R.id.txtRestaurantInfo)).setText(restaurantInfo);

                // Original price (grey, crossed out) only when there is a real discount
                TextView txtOriginalPrice = card.findViewById(R.id.txtOriginalPrice);
                if (f.originalPrice > f.price) {
                    txtOriginalPrice.setVisibility(View.VISIBLE);
                    txtOriginalPrice.setText(PriceFormatter.format(f.originalPrice));
                    txtOriginalPrice.setPaintFlags(
                            txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    txtOriginalPrice.setVisibility(View.GONE);
                }
                ((TextView) card.findViewById(R.id.txtPrice)).setText(PriceFormatter.format(f.price));
                ((TextView) card.findViewById(R.id.txtItemsLeft))
                        .setText(getString(R.string.fmtQuantityLeft, f.quantityLeft));

                card.setOnClickListener(v -> openFood(r.id, f.id));
                llNearby.addView(card);
                shown++;
            }
        }
        txtNoNearby.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
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

            // Original price (grey, crossed out) only when there is a real discount
            TextView txtOriginalPrice = card.findViewById(R.id.txtOriginalPrice);
            if (e.food.originalPrice > e.food.price) {
                txtOriginalPrice.setVisibility(View.VISIBLE);
                txtOriginalPrice.setText(PriceFormatter.format(e.food.originalPrice));
                txtOriginalPrice.setPaintFlags(
                        txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                txtOriginalPrice.setVisibility(View.GONE);
            }
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