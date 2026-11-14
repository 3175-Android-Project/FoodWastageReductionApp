package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CustomerProfile;


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

/**
 * Order detail for finished orders (completed, rejected or cancelled).
 * Orders that are still in progress use CustomerOrderStatusActivity instead.
 */
public class CustomerOrderDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        OrderStore.PlacedOrder placed =
                OrderStore.find(getIntent().getStringExtra(CustomerOrderStatusActivity.EXTRA_ORDER_NUMBER));
        if (placed == null) {
            finish();
            return;
        }
        Order order = placed.order;

        setContentView(R.layout.activity_customer_order_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(getString(R.string.fmtOrderNumber, order.orderNumber));
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Summary card
        ((TextView) findViewById(R.id.txtRestaurantName)).setText(order.restaurantName);
        ((TextView) findViewById(R.id.txtOrderDate)).setText(
                getString(R.string.fmtDateTime, order.date, order.time));

        TextView txtStatus = findViewById(R.id.txtStatus);
        txtStatus.setText(order.status);
        txtStatus.setTextColor(ContextCompat.getColor(this, OrderStatus.textColor(order.status)));
        txtStatus.setBackgroundTintList(
                ContextCompat.getColorStateList(this, OrderStatus.pillBackground(order.status)));

        // Customer (from the profile, replace with the logged-in user later)
        ((TextView) findViewById(R.id.txtCustomerName)).setText(CustomerProfile.fullName);
        ((TextView) findViewById(R.id.txtCustomerContact)).setText(
                getString(R.string.fmtContact, CustomerProfile.email, CustomerProfile.phone));

        // Pickup or delivery method + time
        ((TextView) findViewById(R.id.txtMethod)).setText(
                placed.isDelivery ? R.string.txtMethodDelivery : R.string.txtMethodPickup);
        ((TextView) findViewById(R.id.txtPickupTime)).setText(placed.pickupTime);

        // Items
        LinearLayout llItems = findViewById(R.id.llItems);
        double subtotal = 0;
        for (CartItem c : placed.items) {
            addItemRow(llItems, c.quantity + " x " + c.item.name, c.getLineTotal());
            subtotal += c.getLineTotal();
        }
        double tax = order.total - subtotal;
        if (tax > 0.004) {
            addItemRow(llItems, getString(R.string.lblTax), tax);
        }

        // Total
        ((TextView) findViewById(R.id.txtTotal)).setText(PriceFormatter.format(order.total));
    }

    private void addItemRow(LinearLayout parent, String name, double price) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_order_line, parent, false);
        ((TextView) row.findViewById(R.id.txtItemName)).setText(name);
        ((TextView) row.findViewById(R.id.txtItemPrice)).setText(PriceFormatter.format(price));
        parent.addView(row);
    }
}
