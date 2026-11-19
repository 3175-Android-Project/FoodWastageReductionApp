package com.project.foodwastagereductionapp.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthNavigator;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.User;

public class ManagerDashboardActivity extends AppCompatActivity {

    private com.facebook.shimmer.ShimmerFrameLayout shimmerViewContainer;
    private View scrollContent;
    private static Restaurant cachedManagerRestaurant = null;

    public static void invalidateCache() {
        cachedManagerRestaurant = null;
    }

    public static Restaurant getCachedRestaurant(long managerId) {
        return cachedManagerRestaurant != null && cachedManagerRestaurant.managerId == managerId
                ? cachedManagerRestaurant : null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        shimmerViewContainer = findViewById(R.id.shimmerViewContainer);
        scrollContent = findViewById(R.id.scrollContent);

        findViewById(R.id.cardRestaurantProfile).setOnClickListener(v -> openProfile());
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setItemActiveIndicatorEnabled(false);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_items) {
                startActivity(new Intent(this, ManageFoodItemsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, IncomingOrdersActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                openProfile();
                return true;
            }
            return false;
        });

        findViewById(R.id.cardManageItems).setOnClickListener(v -> {
            startActivity(new Intent(this, ManageFoodItemsActivity.class));
        });

        findViewById(R.id.cardActiveOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, IncomingOrdersActivity.class));
        });

        findViewById(R.id.cardOrderHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SessionManager session = new SessionManager(this);
        SessionManager.Snapshot snapshot = session.snapshot();
        long userId = snapshot.userId;
        Restaurant cached = getCachedRestaurant(userId);
        boolean isColdLoad = cached == null;
        if (isColdLoad) {
            shimmerViewContainer.setVisibility(View.VISIBLE);
            shimmerViewContainer.startShimmer();
            scrollContent.setVisibility(View.GONE);
        } else {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            scrollContent.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtRestaurantName)).setText(cached.name);
        }

        long startTime = System.currentTimeMillis();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            User user = db.getUser(userId);
            if (user == null || !user.emailVerified || !User.MANAGER.equals(user.role)) return null;
            return db.getRestaurantForManager(userId);
        }, restaurant -> {
            if (restaurant == null) {
                AuthNavigator.logout(this);
                return;
            }
            cachedManagerRestaurant = restaurant;
            long elapsed = System.currentTimeMillis() - startTime;
            long delay = isColdLoad ? Math.max(0, 350 - elapsed) : 0;
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (isFinishing() || isDestroyed() || !session.isCurrent(snapshot)) return;
                shimmerViewContainer.stopShimmer();
                shimmerViewContainer.setVisibility(View.GONE);
                scrollContent.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.txtRestaurantName)).setText(restaurant.name);
            }, delay);
        });
    }

    private void openProfile() {
        startActivity(new Intent(this, ManagerProfileActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
