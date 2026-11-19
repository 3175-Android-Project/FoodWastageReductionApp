package com.project.foodwastagereductionapp.customer;

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
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Orders tab: current (in-progress) orders on top, past orders below.
 */
public class CustomerOrderActivity extends AppCompatActivity {

    private final OrderAdapter adapter = new OrderAdapter();
    private TextView txtEmpty;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerViewContainer;
    private RecyclerView rvOrders;
    private static List<Order> cachedOrders = null;
    private static long cachedCustomerId = -1;
    public static void invalidateCache() { cachedOrders = null; cachedCustomerId = -1; }
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

        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setAdapter(adapter);
        txtEmpty = findViewById(R.id.txtEmpty);
        shimmerViewContainer = findViewById(R.id.shimmerViewContainer);

        if (cachedOrders != null && cachedCustomerId == new SessionManager(this).getUserId()) {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
            showOrders(cachedOrders);
        } else {
            shimmerViewContainer.setVisibility(View.VISIBLE);
            shimmerViewContainer.startShimmer();
            rvOrders.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.GONE);
        }

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
        long startTime = System.currentTimeMillis();
        SessionManager session = new SessionManager(this);
        SessionManager.Snapshot snapshot = session.snapshot();
        long customerId = snapshot.userId;
        boolean isColdLoad = cachedOrders == null || cachedCustomerId != customerId;
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            OrderStatusSimulator.catchUp(db, customerId); // demo restaurant (see that class)
            return db.getOrders(customerId);
        }, orders -> {
            cachedOrders = orders;
            cachedCustomerId = customerId;
            long elapsed = System.currentTimeMillis() - startTime;
            long delay = isColdLoad ? Math.max(0, 350 - elapsed) : 0;
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (isFinishing() || isDestroyed() || !session.isCurrent(snapshot)) return;
                shimmerViewContainer.stopShimmer();
                shimmerViewContainer.setVisibility(View.GONE);
                rvOrders.setVisibility(View.VISIBLE);
                showOrders(orders);
            }, delay);
        });
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
        txtEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}
