package com.project.foodwastagereductionapp.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
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
import com.project.foodwastagereductionapp.data.OrderEntity;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import com.project.foodwastagereductionapp.ui.ConfirmationDialog;

public class IncomingOrdersActivity extends BaseActivity {

    private IncomingOrdersAdapter adapter;
    private TextView tabPending, tabAccepted, tabReady, txtEmpty;
    private List<OrderEntity> allOrders = new ArrayList<>();
    private String currentStatus = OrderStatus.PENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_incoming_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.titleIncomingOrders);
        findViewById(R.id.btnBack).setVisibility(View.GONE);

        tabPending = findViewById(R.id.tabPending);
        tabAccepted = findViewById(R.id.tabAccepted);
        tabReady = findViewById(R.id.tabReady);
        txtEmpty = findViewById(R.id.txtEmpty);

        tabPending.setOnClickListener(v -> selectTab(OrderStatus.PENDING));
        tabAccepted.setOnClickListener(v -> selectTab(OrderStatus.ACCEPTED));
        tabReady.setOnClickListener(v -> selectTab(OrderStatus.READY));

        RecyclerView rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncomingOrdersAdapter(new IncomingOrdersAdapter.OnOrderAction() {
            @Override
            public void onChangeStatus(OrderEntity order, String newStatus) {
                if (OrderStatus.REJECTED.equals(newStatus)) {
                    confirmReject(order);
                } else {
                    changeStatus(order, newStatus);
                }
            }

            @Override
            public void onSendReminder(OrderEntity order) {
                sendReminder(order);
            }
        });
        rvOrders.setAdapter(adapter);

        setupBottomNavigation();
        updateTabStyles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void selectTab(String status) {
        currentStatus = status;
        updateTabStyles();
        filterOrders();
    }

    private void updateTabStyles() {
        tabPending.setBackground(null);
        tabAccepted.setBackground(null);
        tabReady.setBackground(null);
        tabPending.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabAccepted.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabReady.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabPending.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabAccepted.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabReady.setTypeface(null, android.graphics.Typeface.NORMAL);

        TextView active = tabPending;
        if (OrderStatus.ACCEPTED.equals(currentStatus)) {
            active = tabAccepted;
        } else if (OrderStatus.READY.equals(currentStatus)) {
            active = tabReady;
        }

        active.setBackgroundResource(R.drawable.bg_tab_pill);
        active.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.background_accent1));
        active.setTextColor(ContextCompat.getColor(this, R.color.text_inverse));
        active.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void loadOrders() {
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            Restaurant restaurant = db.getRestaurantForManager(userId);
            if (restaurant == null) return new ArrayList<OrderEntity>();
            return db.getOrderEntitiesForRestaurant(restaurant.id);
        }, orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            filterOrders();
        });
    }

    private void filterOrders() {
        List<OrderEntity> filtered = new ArrayList<>();
        for (OrderEntity o : allOrders) {
            // The Accepted tab also shows orders that are being prepared
            boolean preparingInAccepted = OrderStatus.ACCEPTED.equals(currentStatus)
                    && OrderStatus.PREPARING.equalsIgnoreCase(o.status);
            if (currentStatus.equalsIgnoreCase(o.status) || preparingInAccepted) {
                filtered.add(o);
            }
        }
        adapter.setRows(filtered);
        txtEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
    // ---- Order actions -----------------------------------------------------------------

    private void confirmReject(OrderEntity order) {
        ConfirmationDialog.show(this,
                getString(R.string.dialogRejectOrderTitle),
                getString(R.string.dialogRejectOrderMessage),
                getString(R.string.btnRejectOrder),
                getString(R.string.btnCancel),
                () -> changeStatus(order, OrderStatus.REJECTED));
    }

    /** Saves the new status (the customer gets a notification) and reloads the list. */
    private void changeStatus(OrderEntity order, String newStatus) {
        long managerId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null,
                () -> db.updateOrderStatusForRestaurant(managerId, order.id, newStatus),
                changed -> {
                    if (!changed) {
                        // e.g. the customer cancelled it a moment ago
                        Toast.makeText(this, R.string.msgOrderChangedReload, Toast.LENGTH_SHORT).show();
                    }
                    loadOrders();
                });
    }

    private void sendReminder(OrderEntity order) {
        long managerId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null,
                () -> db.sendPickupReminder(managerId, order.id),
                sent -> {
                    Toast.makeText(this, sent ? R.string.msgReminderSent : R.string.msgOrderChangedReload,
                            Toast.LENGTH_SHORT).show();
                    if (!sent) loadOrders();
                });
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemActiveIndicatorEnabled(false);
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_orders) return true;
            if (id == R.id.nav_home) {
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
