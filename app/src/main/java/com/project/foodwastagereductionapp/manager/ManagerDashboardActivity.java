package com.project.foodwastagereductionapp.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthNavigator;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.OrderEntity;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.User;

import java.util.List;

public class ManagerDashboardActivity extends AppCompatActivity {

    private com.facebook.shimmer.ShimmerFrameLayout shimmerViewContainer;
    private View scrollContent;

    private static class DashboardData {
        final Restaurant restaurant;
        final int pending;
        final int accepted;
        final int ready;
        DashboardData(Restaurant restaurant, int pending, int accepted, int ready) {
            this.restaurant = restaurant;
            this.pending = pending;
            this.accepted = accepted;
            this.ready = ready;
        }
    }

    private static DashboardData cachedDashboardData = null;
    private static Restaurant cachedManagerRestaurant = null;

    public static void invalidateCache() {
        cachedDashboardData = null;
        cachedManagerRestaurant = null;
    }

    public static Restaurant getCachedRestaurant(long managerId) {
        if (cachedDashboardData != null && cachedDashboardData.restaurant.managerId == managerId) {
            return cachedDashboardData.restaurant;
        }
        return cachedManagerRestaurant != null && cachedManagerRestaurant.managerId == managerId
                ? cachedManagerRestaurant : null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        shimmerViewContainer = findViewById(R.id.shimmerViewContainer);
        scrollContent = findViewById(R.id.scrollContent);

        findViewById(R.id.btnNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, ManagerNotificationsActivity.class));
        });

        findViewById(R.id.cardRestaurantProfile).setOnClickListener(v -> openProfile());
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setItemActiveIndicatorEnabled(false);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_items) {
                startActivity(new Intent(this, ManageFoodItemsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, IncomingOrdersActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                openProfile();
                return true;
            }
            return false;
        });

        findViewById(R.id.cardPending).setOnClickListener(v -> {
            Intent intent = new Intent(this, IncomingOrdersActivity.class);
            intent.putExtra("status", OrderStatus.PENDING);
            startActivity(intent);
        });
        findViewById(R.id.cardAccepted).setOnClickListener(v -> {
            Intent intent = new Intent(this, IncomingOrdersActivity.class);
            intent.putExtra("status", OrderStatus.ACCEPTED);
            startActivity(intent);
        });
        findViewById(R.id.cardReady).setOnClickListener(v -> {
            Intent intent = new Intent(this, IncomingOrdersActivity.class);
            intent.putExtra("status", OrderStatus.READY);
            startActivity(intent);
        });

        findViewById(R.id.cardManageItems).setOnClickListener(v -> {
            startActivity(new Intent(this, ManageFoodItemsActivity.class));
        });

        findViewById(R.id.cardActiveOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, IncomingOrdersActivity.class));
        });

        findViewById(R.id.cardOrderHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SessionManager session = new SessionManager(this);
        SessionManager.Snapshot snapshot = session.snapshot();
        long userId = snapshot.userId;
        DashboardData cached = cachedDashboardData;
        boolean isColdLoad = cached == null || cached.restaurant.managerId != userId;

        if (isColdLoad) {
            shimmerViewContainer.setVisibility(View.VISIBLE);
            shimmerViewContainer.startShimmer();
            scrollContent.setVisibility(View.GONE);
        } else {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            scrollContent.setVisibility(View.VISIBLE);
            bindDashboard(cached);
        }

        checkUnreadNotifications();

        long startTime = System.currentTimeMillis();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            User user = db.getUser(userId);
            if (user == null || !user.emailVerified || !User.MANAGER.equals(user.role)) return null;
            Restaurant restaurant = db.getRestaurantForManager(userId);
            if (restaurant == null) return null;

            List<OrderEntity> orders = db.getOrderEntitiesForRestaurant(restaurant.id);
            int pending = 0;
            int accepted = 0;
            int ready = 0;
            for (OrderEntity o : orders) {
                String st = o.status == null ? "" : o.status;
                if (OrderStatus.PENDING.equals(st)) {
                    pending++;
                } else if (OrderStatus.ACCEPTED.equals(st) || OrderStatus.PREPARING.equals(st)) {
                    accepted++;
                } else if (OrderStatus.READY.equals(st)) {
                    ready++;
                }
            }
            return new DashboardData(restaurant, pending, accepted, ready);
        }, data -> {
            if (data == null) {
                AuthNavigator.logout(this);
                return;
            }
            cachedDashboardData = data;
            cachedManagerRestaurant = data.restaurant;
            long elapsed = System.currentTimeMillis() - startTime;
            long delay = isColdLoad ? Math.max(0, 350 - elapsed) : 0;
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (isFinishing() || isDestroyed() || !session.isCurrent(snapshot)) return;
                shimmerViewContainer.stopShimmer();
                shimmerViewContainer.setVisibility(View.GONE);
                scrollContent.setVisibility(View.VISIBLE);
                bindDashboard(data);
            }, delay);
        });
    }

    private void checkUnreadNotifications() {
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            List<AppNotification> notifs = db.getManagerNotifications(userId);
            if (notifs.isEmpty()) return false;
            long lastSeen = getSharedPreferences("grabbit_notifications_prefs", MODE_PRIVATE)
                    .getLong("last_seen_notif_" + userId, 0);
            for (AppNotification n : notifs) {
                if (n.timeMillis > lastSeen) return true;
            }
            return false;
        }, hasUnread -> {
            if (hasUnread != null) {
                findViewById(R.id.notificationBadge).setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void bindDashboard(DashboardData data) {
        ((TextView) findViewById(R.id.txtRestaurantName)).setText(data.restaurant.name);
        ((TextView) findViewById(R.id.txtPendingCount)).setText(String.valueOf(data.pending));
        ((TextView) findViewById(R.id.txtAcceptedCount)).setText(String.valueOf(data.accepted));
        ((TextView) findViewById(R.id.txtReadyCount)).setText(String.valueOf(data.ready));
    }

    private void openProfile() {
        startActivity(new Intent(this, ManagerProfileActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
