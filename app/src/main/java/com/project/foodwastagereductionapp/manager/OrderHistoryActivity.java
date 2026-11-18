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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.Restaurant;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerOrders;
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                });

        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.txtOrderHistoryTitle);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        recyclerOrders = findViewById(R.id.recyclerOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter();
        recyclerOrders.setAdapter(adapter);
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderHistory();
    }

    private void loadOrderHistory() {
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);

        AuthTasks.run(this, null, () -> {
                    Restaurant restaurant = db.getRestaurantForManager(userId);
                    if (restaurant == null) {
                        return null;
                    }

                    return db.getOrderHistory(
                            restaurant.id
                    );
                },

                orders -> {
                    if (orders == null) {
                        return;
                    }
                    adapter.setRows(orders);
                }
        );
    }

    private void setupBottomNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setItemActiveIndicatorEnabled(false);

        bottomNav.setSelectedItemId(R.id.nav_orders);

        bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_orders) {
                        return true;
                    } else if (id == R.id.nav_home) {
                        startActivity(new Intent(this, ManagerDashboardActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.nav_items) {
                        startActivity(new Intent(this, ManageFoodItemsActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(this, ManagerProfileActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    }
                    return false;
        });
    }
}