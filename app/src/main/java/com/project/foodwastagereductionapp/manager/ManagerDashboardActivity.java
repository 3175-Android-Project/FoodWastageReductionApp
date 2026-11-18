package com.project.foodwastagereductionapp.manager;

import android.content.Intent;
import android.os.Bundle;
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
import com.project.foodwastagereductionapp.customer.CustomerBrowseActivity;
import com.project.foodwastagereductionapp.customer.CustomerOrderActivity;
import com.project.foodwastagereductionapp.customer.CustomerProfileActivity;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;

public class ManagerDashboardActivity extends AppCompatActivity {

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
        findViewById(R.id.cardRestaurantProfile).setOnClickListener(v -> openProfile());
        BottomNavigationView nav = findViewById(R.id.bottomNav);
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
                startActivity(new Intent(this, OrderHistoryActivity.class));
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

        findViewById(R.id.cardOrderHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
                });
    }

    @Override protected void onResume() {
        super.onResume();
        long userId = new SessionManager(this).getUserId();
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
            ((TextView) findViewById(R.id.txtRestaurantName)).setText(restaurant.name);
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // already here
            } else if (id == R.id.nav_items) {
                goTo(ManageFoodItemsActivity.class);
                return true;
            } else if (id == R.id.nav_orders) {
                goTo(OrderHistoryActivity.class);
                return true;
            } else if (id == R.id.nav_profile) {
                goTo(ManagerProfileActivity.class);
                return true;
            }
            return false;
        });
    }

    private void openProfile() {
        startActivity(new Intent(this, ManagerProfileActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}
