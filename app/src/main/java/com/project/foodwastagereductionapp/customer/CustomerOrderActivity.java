package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CustomerProfile;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Orders tab: current (in-progress) orders on top, past orders below.
 */
public class CustomerOrderActivity extends AppCompatActivity {

    private final OrderAdapter adapter = new OrderAdapter();
    // Keeps the list up to date when an order's status changes while this screen is open
    private final OrderStore.Listener orderListener = this::refreshOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.txtOrdersTitle);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> goTo(CustomerHomeActivity.class));

        RecyclerView rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setAdapter(adapter);

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_orders) return true;
            if (id == R.id.nav_home)    { goTo(CustomerHomeActivity.class);    return true; }
            if (id == R.id.nav_search)  { goTo(CustomerBrowseActivity.class);  return true; }
            if (id == R.id.nav_profile) { goTo(CustomerProfileActivity.class); return true; }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        OrderStore.addListener(orderListener);
        refreshOrders(); // also picks up changes made while another screen was open
    }

    @Override
    protected void onStop() {
        super.onStop();
        OrderStore.removeListener(orderListener);
    }

    /** Splits the orders into "Current orders" and "Past orders" sections. */
    private void refreshOrders() {
        List<Order> current = new ArrayList<>();
        List<Order> past = new ArrayList<>();
        for (Order o : OrderStore.getOrders()) { // newest first
            if (OrderStatus.isActive(o.status)) current.add(o);
            else past.add(o);
        }

        List<Object> rows = new ArrayList<>();
        if (!current.isEmpty()) {
            rows.add(getString(R.string.titleCurrentOrders));
            rows.addAll(current);
        }
        if (!past.isEmpty()) {
            rows.add(getString(R.string.titlePastOrders));
            rows.addAll(past);
        }
        adapter.setRows(rows);
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}
