package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;

public class CustomerHomeActivity extends AppCompatActivity {

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

        // Browse: search box and "See all" open the Browse screen
        findViewById(R.id.editSearch).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerBrowseActivity.class)));
        findViewById(R.id.txtSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerBrowseActivity.class)));

        // Bell opens the order notifications
        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // Restaurant cards open the restaurant's food items (ids match SampleData)
        findViewById(R.id.cardRestaurant1).setOnClickListener(v -> openRestaurant(1));
        findViewById(R.id.cardRestaurant2).setOnClickListener(v -> openRestaurant(2));
        findViewById(R.id.cardEndingSoon).setOnClickListener(v -> openRestaurant(3));

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

    private void openRestaurant(int restaurantId) {
        Intent intent = new Intent(this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}