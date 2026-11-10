package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.MainActivity;
import com.project.foodwastagereductionapp.R;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView txtCustomerName, txtEmail, txtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        // Header title
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.txtProfile);

        // Back button → Home
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> goToHome());

        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtEmail = findViewById(R.id.txtEmail);
        txtAddress = findViewById(R.id.txtAddress);

        // Edit Profile row → Edit Profile screen
        LinearLayout layoutEditProfile = findViewById(R.id.layoutEditProfile);
        layoutEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

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
        txtCustomerName.setText(CustomerProfile.fullName);
        txtEmail.setText(CustomerProfile.email);

        String address = CustomerProfile.getAddressLine();
        txtAddress.setText(address.isEmpty() ? getString(R.string.txtNoAddress) : address);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialogLogoutTitle)
                .setMessage(R.string.dialogLogoutMessage)
                .setPositiveButton(R.string.btnLogoutConfirm, (dialog, which) -> logout())
                .setNegativeButton(R.string.btnCancel, null)
                .show();
    }

    private void logout() {
        // TODO: sign out of the real account later
        CartManager.clear();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToHome() {
        startActivity(new Intent(this, CustomerHomeActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

}
