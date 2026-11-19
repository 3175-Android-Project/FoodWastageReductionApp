package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthNavigator;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.ChangePasswordActivity;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.ui.ConfirmationDialog;

public class CustomerProfileActivity extends AppCompatActivity {

    private static User cachedUser = null;

    public static void invalidateCache() {
        cachedUser = null;
    }

    private ShimmerFrameLayout shimmerViewContainer;
    private View scrollContent;
    private TextView txtCustomerName, txtEmail, txtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        shimmerViewContainer = findViewById(R.id.shimmerViewContainer);
        scrollContent = findViewById(R.id.scrollContent);

        // Header title
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.txtProfile);

        findViewById(R.id.btnBack).setVisibility(View.GONE);

        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtEmail = findViewById(R.id.txtEmail);
        txtAddress = findViewById(R.id.txtAddress);

        // Edit Profile row → Edit Profile screen
        LinearLayout layoutEditProfile = findViewById(R.id.layoutEditProfile);
        layoutEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        LinearLayout layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutChangePassword.setEnabled(false);
        layoutChangePassword.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        // Logout row → confirm, then back to the landing screen
        LinearLayout layoutLogout = findViewById(R.id.layoutLogout);
        layoutLogout.setOnClickListener(v -> confirmLogout());

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                return true; // already here
            } else if (id == R.id.nav_home) {
                goToHome();
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, CustomerBrowseActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, CustomerOrderActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProfile(); // refresh after returning from Edit Profile
    }

    private void showProfile() {
        SessionManager session = new SessionManager(this);
        SessionManager.Snapshot snapshot = session.snapshot();
        long userId = snapshot.userId;

        View editProfile = findViewById(R.id.layoutEditProfile);
        View changePassword = findViewById(R.id.layoutChangePassword);

        boolean isColdLoad = (cachedUser == null || cachedUser.id != userId);

        if (isColdLoad) {
            shimmerViewContainer.setVisibility(View.VISIBLE);
            shimmerViewContainer.startShimmer();
            scrollContent.setVisibility(View.GONE);
            editProfile.setEnabled(false);
            changePassword.setEnabled(false);
        } else {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            scrollContent.setVisibility(View.VISIBLE);
            bindProfile(cachedUser);
            editProfile.setEnabled(true);
            changePassword.setEnabled(true);
        }

        long startTime = System.currentTimeMillis();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            User user = db.getUser(userId);
            if (user == null || !user.emailVerified || !User.CUSTOMER.equals(user.role)) return null;
            return user;
        }, user -> {
            if (user == null) {
                AuthNavigator.logout(this);
                return;
            }
            cachedUser = user;
            long elapsed = System.currentTimeMillis() - startTime;
            long delay = isColdLoad ? Math.max(0, 350 - elapsed) : 0;
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (isFinishing() || isDestroyed() || !session.isCurrent(snapshot)) return;
                shimmerViewContainer.stopShimmer();
                shimmerViewContainer.setVisibility(View.GONE);
                scrollContent.setVisibility(View.VISIBLE);
                bindProfile(user);
                editProfile.setEnabled(true);
                changePassword.setEnabled(true);
            }, delay);
        });
    }

    private void bindProfile(User user) {
        com.project.foodwastagereductionapp.profile.ProfilePhotoStore.show(
                findViewById(R.id.imgAvatar), user.profilePhotoUri);
        txtCustomerName.setText(user.fullName);
        txtEmail.setText(user.email);
        if (user.hasAddress()) {
            txtAddress.setText(getString(R.string.fmtFullAddress,
                    user.address, user.city, user.province, user.postalCode));
        } else {
            txtAddress.setText(getString(R.string.profileLocation,
                    user.city, user.province, user.postalCode));
        }
    }

    private void confirmLogout() {
        ConfirmationDialog.showLogout(this, this::logout);
    }

    private void logout() {
        CartManager.clear();
        AuthNavigator.logout(this);
    }

    private void goToHome() {
        startActivity(new Intent(this, CustomerHomeActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
