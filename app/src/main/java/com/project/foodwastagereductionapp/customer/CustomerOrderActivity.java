package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Orders tab: current (in-progress) orders on top, past orders below.
 */
public class CustomerOrderActivity extends AppCompatActivity {

    private final OrderAdapter adapter = new OrderAdapter();
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    // Re-reads the orders every few seconds so status changes show up while the screen is open
    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            refreshOrders();
            handler.postDelayed(this, OrderStatusSimulator.POLL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.txtOrdersTitle);

        findViewById(R.id.btnBack).setVisibility(View.GONE);

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
        handler.post(poll); // also picks up changes made while another screen was open
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(poll);
    }

    /** Splits the orders into "Current orders" and "Past orders" sections. */
    private void refreshOrders() {
        long customerId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            OrderStatusSimulator.catchUp(db, customerId); // demo restaurant (see that class)
            return db.getOrders(customerId);
        }, this::showOrders);
    }

    private void showOrders(List<Order> orders) {
        List<Order> current = new ArrayList<>();
        List<Order> past = new ArrayList<>();
        for (Order o : orders) { // newest first
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
