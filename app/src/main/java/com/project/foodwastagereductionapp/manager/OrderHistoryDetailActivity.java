package com.project.foodwastagereductionapp.manager;

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
import com.project.foodwastagereductionapp.customer.PriceFormatter;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderDetails;
import com.project.foodwastagereductionapp.data.OrderLine;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.Restaurant;

public class OrderHistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history_detail);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        long orderId = getIntent().getLongExtra("orderId", -1);
        long managerId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);

        AuthTasks.run(this, null, () -> {
                    Restaurant restaurant = db.getRestaurantForManager(managerId);
                    if (restaurant == null) {
                        return null;
                    }

                    return db.getOrderDetailsForManager(restaurant.id, orderId);
                },

                details -> {
                    if (details == null) {
                        finish();
                        return;
                    }
                    bind(details);
                }
        );

    }

    private void bind(OrderDetails details) {

        Order order = details.order;

        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);

        txtHeaderTitle.setText(getString(R.string.fmtOrderNumber, order.orderNumber));

        ((TextView) findViewById(R.id.txtRestaurantName)).setText(order.restaurantName);

        ((TextView) findViewById(R.id.txtOrderDate)).setText(getString(R.string.fmtDateTime, order.date, order.time));

        TextView txtStatus = findViewById(R.id.txtStatus);

        txtStatus.setText(order.status);

        txtStatus.setTextColor(ContextCompat.getColor(this, OrderStatus.textColor(order.status)));

        txtStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, OrderStatus.pillBackground(order.status)));

        ((TextView) findViewById(R.id.txtCustomerName)).setText(details.customerName);

        ((TextView) findViewById(R.id.txtCustomerContact)).setText(details.customerPhone.isEmpty() ? details.customerEmail : getString(R.string.fmtContact, details.customerEmail, details.customerPhone));

        ((TextView) findViewById(R.id.txtMethod)).setText(details.isDelivery ? R.string.txtMethodDelivery : R.string.txtMethodPickup);

        ((TextView) findViewById(R.id.txtPickupTime)).setText(details.pickupTime);

        LinearLayout llItems = findViewById(R.id.llItems);

        llItems.removeAllViews();

        for (OrderLine line : details.lines) {
            addItemRow(llItems, line.quantity + " x " + line.name, line.getLineTotal());
        }

        double tax = order.total - details.getSubtotal();

        if (tax > 0.004) {
            addItemRow(llItems, getString(R.string.lblTax), tax);
        }

        ((TextView) findViewById(R.id.txtTotal)).setText(PriceFormatter.format(order.total));
    }

    private void addItemRow(LinearLayout parent, String name, double price) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_order_line, parent, false);
        ((TextView) row.findViewById(R.id.txtItemName)).setText(name);
        ((TextView) row.findViewById(R.id.txtItemPrice)).setText(PriceFormatter.format(price));
        parent.addView(row);
    }
}