package com.project.foodwastagereductionapp.manager;

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

public class ManagerNotificationsActivity extends AppCompatActivity {

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

        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleNotifications);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    @Override
    protected void onStart() {
        super.onStart();
        long managerId = new SessionManager(this).getUserId();
        getSharedPreferences("grabbit_notifications_prefs", MODE_PRIVATE)
                .edit()
                .putLong("last_seen_notif_" + managerId, System.currentTimeMillis())
                .apply();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> db.getManagerNotifications(managerId), notifications -> {
            rvNotifications.setAdapter(new ManagerNotificationAdapter(notifications));
            txtEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
