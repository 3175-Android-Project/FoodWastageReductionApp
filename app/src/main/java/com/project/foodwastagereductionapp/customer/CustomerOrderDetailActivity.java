package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;


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
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.OrderDetails;
import com.project.foodwastagereductionapp.data.OrderLine;

/**
 * Order detail for finished orders (completed, rejected or cancelled).
 * Orders that are still in progress use CustomerOrderStatusActivity instead.
 */
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

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        long orderId = getIntent().getLongExtra(CustomerOrderStatusActivity.EXTRA_ORDER_ID, -1);
        long customerId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            OrderStatusSimulator.catchUp(db, customerId); // demo restaurant (see that class)
            return db.getOrderDetails(customerId, orderId);
        }, details -> {
            if (details == null) {
                finish();
                return;
            }
            bind(details);
        });
    }

    private void bind(OrderDetails details) {
        Order order = details.order;

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(getString(R.string.fmtOrderNumber, order.orderNumber));

        // Summary card
        ((TextView) findViewById(R.id.txtRestaurantName)).setText(order.restaurantName);
        ((TextView) findViewById(R.id.txtOrderDate)).setText(
                getString(R.string.fmtDateTime, order.date, order.time));

        TextView txtStatus = findViewById(R.id.txtStatus);
        txtStatus.setText(order.status);
        txtStatus.setTextColor(ContextCompat.getColor(this, OrderStatus.textColor(order.status)));
        txtStatus.setBackgroundTintList(
                ContextCompat.getColorStateList(this, OrderStatus.pillBackground(order.status)));

        // Customer (saved with the account)
        ((TextView) findViewById(R.id.txtCustomerName)).setText(details.customerName);
        ((TextView) findViewById(R.id.txtCustomerContact)).setText(
                details.customerPhone.isEmpty() ? details.customerEmail
                        : getString(R.string.fmtContact, details.customerEmail, details.customerPhone));

        // Pickup or delivery method + time
        ((TextView) findViewById(R.id.txtMethod)).setText(
                details.isDelivery ? R.string.txtMethodDelivery : R.string.txtMethodPickup);
        ((TextView) findViewById(R.id.txtPickupTime)).setText(details.pickupTime);

        // Items
        LinearLayout llItems = findViewById(R.id.llItems);
        for (OrderLine l : details.lines) {
            addItemRow(llItems, l.quantity + " x " + l.name, l.getLineTotal());
        }
        double tax = order.total - details.getSubtotal();
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
