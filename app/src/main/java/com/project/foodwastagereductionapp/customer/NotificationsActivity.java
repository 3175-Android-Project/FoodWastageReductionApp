package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;

import java.util.List;

/**
 * Order-status updates and pickup reminders, newest first. Opened from the bell on Home.
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private View txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleNotifications);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Reload every time so updates that arrived while away (e.g. on the status screen) show up
        long customerId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            OrderStatusSimulator.catchUp(db, customerId); // demo restaurant (see that class)
            return db.getNotifications(customerId);
        }, notifications -> {
            rvNotifications.setAdapter(new NotificationAdapter(notifications));
            txtEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
