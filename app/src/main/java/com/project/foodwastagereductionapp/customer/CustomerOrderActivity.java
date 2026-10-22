package com.project.foodwastagereductionapp.customer;

import static androidx.core.content.ContextCompat.startActivity;

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

public class CustomerOrderActivity extends AppCompatActivity {
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
        txtHeaderTitle.setText(R.string.txtOrderHistoryTitle);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> goTo(CustomerHomeActivity.class));

        // Sample data (replace with database later)
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("1024", "Awesome sushi", "Oct 1, 2026", 1, 8.50, "Completed", R.drawable.icon_food));
        orders.add(new Order("1023", "Happy sushi",   "Oct 1, 2026", 1, 8.50, "Rejected",  R.drawable.icon_food));

        RecyclerView rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setAdapter(new OrderAdapter(orders));

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_orders) return true;
            if (id == R.id.nav_home)    { goTo(CustomerHomeActivity.class);    return true; }
            if (id == R.id.nav_profile) { goTo(CustomerProfileActivity.class); return true; }
            return false;
        });
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}
