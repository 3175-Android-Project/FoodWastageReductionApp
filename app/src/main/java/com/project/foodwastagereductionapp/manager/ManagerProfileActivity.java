package com.project.foodwastagereductionapp.manager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
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
import com.project.foodwastagereductionapp.auth.RestaurantInformationActivity;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.ui.ConfirmationDialog;

public class ManagerProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.txtProfile);
        findViewById(R.id.btnBack).setVisibility(View.GONE);
        findViewById(R.id.layoutEditProfile).setEnabled(false);
        findViewById(R.id.layoutEditProfile).setOnClickListener(view -> {
            Intent intent = new Intent(this, RestaurantInformationActivity.class);
            intent.putExtra(RestaurantInformationActivity.EXTRA_EDIT_PROFILE, true);
            startActivity(intent);
        });
        findViewById(R.id.layoutLogout).setOnClickListener(
                view -> ConfirmationDialog.showLogout(this, () -> AuthNavigator.logout(this)));

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setItemActiveIndicatorEnabled(false);
        nav.setSelectedItemId(R.id.nav_profile);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;
            if (id == R.id.nav_home) {
                goHome();
                return true;
            } else if (id == R.id.nav_items) {
                startActivity(new Intent(this, ManageFoodItemsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override protected void onResume() {
        super.onResume();
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        View editProfile = findViewById(R.id.layoutEditProfile);
        editProfile.setEnabled(false);
        AuthTasks.run(this, null, () -> {
            User user = db.getUser(userId);
            if (user == null || !user.emailVerified || !User.MANAGER.equals(user.role)) return null;
            return db.getRestaurantForManager(userId);
        }, restaurant -> {
            if (restaurant == null) {
                AuthNavigator.logout(this);
                return;
            }
            ImageView image = findViewById(R.id.imgManagerProfile);
            if (restaurant.imageUri == null || restaurant.imageUri.isEmpty()) {
                image.setImageResource(R.drawable.icon_store);
                image.setImageTintList(ColorStateList.valueOf(
                        getColor(R.color.text_primary)
                ));
                image.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.space_xl),
                        getResources().getDimensionPixelSize(R.dimen.space_xl),
                        getResources().getDimensionPixelSize(R.dimen.space_xl),
                        getResources().getDimensionPixelSize(R.dimen.space_xl)
                );
            } else {
                image.setImageURI(Uri.parse(restaurant.imageUri));
                image.setPadding(0, 0, 0, 0);
                image.setImageTintList(null);
            }
            ((TextView) findViewById(R.id.txtRestaurantName)).setText(restaurant.name);
            editProfile.setEnabled(true);
        });
    }

    private void goHome() {
        Intent intent = new Intent(this, ManagerDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
