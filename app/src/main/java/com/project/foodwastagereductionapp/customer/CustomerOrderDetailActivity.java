package com.project.foodwastagereductionapp.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;

import java.util.Locale;

public class CustomerOrderDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_order_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        // Data from the clicked order
        String orderNumber    = getIntent().getStringExtra("orderNumber");
        String restaurantName = getIntent().getStringExtra("restaurantName");
        String date           = getIntent().getStringExtra("date");
        String status         = getIntent().getStringExtra("status");
        double total          = getIntent().getDoubleExtra("total", 0);

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText("Order #" + orderNumber);
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Summary card
        ((TextView) findViewById(R.id.txtRestaurantName)).setText(restaurantName);
        ((TextView) findViewById(R.id.txtOrderDate)).setText(date + " · 5:30 PM");

        TextView txtStatus = findViewById(R.id.txtStatus);
        txtStatus.setText(status);
        int textColor, bgColor;
        if ("Completed".equals(status) || "Picked up".equals(status)) {
            textColor = R.color.text_success;  bgColor = R.color.pill_green_bg;
        } else if ("Rejected".equals(status)) {
            textColor = R.color.text_warning;  bgColor = R.color.pill_red_bg;
        } else {
            textColor = R.color.text_tertiary; bgColor = R.color.pill_grey_bg;
        }
        txtStatus.setTextColor(ContextCompat.getColor(this, textColor));
        txtStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, bgColor));

        // Customer (sample — replace with logged-in user later)
        ((TextView) findViewById(R.id.txtCustomerName)).setText("Customer Park");
        ((TextView) findViewById(R.id.txtCustomerContact)).setText("a@email.com · 604-555-0123");

        // Method + time (sample)
        ((TextView) findViewById(R.id.txtMethod)).setText("Pickup");
        ((TextView) findViewById(R.id.txtPickupTime)).setText("6:00 PM");

        // Items (sample — replace with real order items later)
        LinearLayout llItems = findViewById(R.id.llItems);
        addItemRow(llItems, "Salmon oshi", total);

        // Total
        ((TextView) findViewById(R.id.txtTotal)).setText(formatPrice(total));
    }

    private void addItemRow(LinearLayout parent, String name, double price) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_order_line, parent, false);
        ((TextView) row.findViewById(R.id.txtItemName)).setText(name);
        ((TextView) row.findViewById(R.id.txtItemPrice)).setText(formatPrice(price));
        parent.addView(row);
    }

    private String formatPrice(double value) {
        return String.format(Locale.CANADA, "$%.2f", value);
    }
}