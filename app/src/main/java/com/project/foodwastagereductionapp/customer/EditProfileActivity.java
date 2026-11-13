package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CustomerProfile;


import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;

public class EditProfileActivity extends AppCompatActivity {

    private EditText inputFullName, etAddress, etCity, etProvince, etPostalCode, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.txtEditProfileTitle);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // back to Profile

        inputFullName = findViewById(R.id.inputFullName);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etPostalCode = findViewById(R.id.etPostalCode);
        etPhone = findViewById(R.id.etPhone);

        // Start with the current profile
        inputFullName.setText(CustomerProfile.fullName);
        etAddress.setText(CustomerProfile.address);
        etCity.setText(CustomerProfile.city);
        etProvince.setText(CustomerProfile.province);
        etPostalCode.setText(CustomerProfile.postalCode);
        etPhone.setText(CustomerProfile.phone);

        // Save Changes
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String fullName = textOf(inputFullName);
        if (fullName.isEmpty()) {
            inputFullName.setError(getString(R.string.msgFullNameRequired));
            return;
        }

        // TODO: save to database later
        CustomerProfile.fullName = fullName;
        CustomerProfile.address = textOf(etAddress);
        CustomerProfile.city = textOf(etCity);
        CustomerProfile.province = textOf(etProvince);
        CustomerProfile.postalCode = textOf(etPostalCode);
        CustomerProfile.phone = textOf(etPhone);

        Toast.makeText(this, R.string.msgProfileSaved, Toast.LENGTH_SHORT).show();
        finish(); // back to Profile
    }

    private String textOf(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
