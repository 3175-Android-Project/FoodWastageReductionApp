package com.project.foodwastagereductionapp.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        txtHeaderTitle.setText(R.string.titleOrderDetails);

        ((TextView) findViewById(R.id.txtRestaurantName)).setText(getString(R.string.fmtOrderNumber, order.orderNumber));
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

        setupActions(details);
    }

    private void setupActions(OrderDetails details) {
        Order order = details.order;
        LinearLayout layoutPending = findViewById(R.id.layoutPendingActions);
        LinearLayout layoutManager = findViewById(R.id.layoutManagerActions);
        Button btnPrimary = findViewById(R.id.btnPrimaryAction);
        Button btnSecondary = findViewById(R.id.btnSecondaryAction);
        Button btnReject = findViewById(R.id.btnReject);
        Button btnAccept = findViewById(R.id.btnAccept);

        if (layoutPending == null || layoutManager == null) return;

        layoutPending.setVisibility(View.GONE);
        layoutManager.setVisibility(View.GONE);
        btnSecondary.setVisibility(View.GONE);

        String status = order.status == null ? "" : order.status;
        switch (status) {
            case OrderStatus.PENDING:
                layoutPending.setVisibility(View.VISIBLE);
                btnReject.setOnClickListener(v -> updateStatus(order.id, OrderStatus.REJECTED));
                btnAccept.setOnClickListener(v -> updateStatus(order.id, OrderStatus.ACCEPTED));
                break;
            case OrderStatus.ACCEPTED:
                layoutManager.setVisibility(View.VISIBLE);
                btnPrimary.setText("Start preparing");
                btnPrimary.setOnClickListener(v -> updateStatus(order.id, OrderStatus.PREPARING));
                break;
            case OrderStatus.PREPARING:
                layoutManager.setVisibility(View.VISIBLE);
                btnPrimary.setText("Mark as ready");
                btnPrimary.setOnClickListener(v -> updateStatus(order.id, OrderStatus.READY));
                break;
            case OrderStatus.READY:
                layoutManager.setVisibility(View.VISIBLE);
                btnSecondary.setVisibility(View.VISIBLE);
                btnSecondary.setText("Send reminder again");
                btnSecondary.setBackgroundResource(R.drawable.bg_card);
                btnSecondary.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                btnSecondary.setOnClickListener(v -> Toast.makeText(this, R.string.msgReminderSent, Toast.LENGTH_SHORT).show());

                btnPrimary.setText(details.isDelivery ? "Mark as delivered" : "Mark as picked up");
                btnPrimary.setOnClickListener(v -> updateStatus(order.id, OrderStatus.COMPLETED));
                break;
            default:
                break;
        }
    }

    private void updateStatus(long orderId, String newStatus) {
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            db.updateOrderStatus(orderId, newStatus);
            return true;
        }, result -> {
            recreate();
        });
    }

    private void addItemRow(LinearLayout parent, String name, double price) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_order_line, parent, false);
        ((TextView) row.findViewById(R.id.txtItemName)).setText(name);
        ((TextView) row.findViewById(R.id.txtItemPrice)).setText(PriceFormatter.format(price));
        parent.addView(row);
    }
}
