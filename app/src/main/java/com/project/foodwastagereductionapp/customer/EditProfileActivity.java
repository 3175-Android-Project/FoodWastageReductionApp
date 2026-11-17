package com.project.foodwastagereductionapp.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthNavigator;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.ui.BaseActivity;

/** Edits the logged-in customer's account (saved in the database). */
public class EditProfileActivity extends BaseActivity {

    private EditText inputFullName, etAddress, etCity, etProvince, etPostalCode, etPhone;
    private Button btnSaveChanges;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
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
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        userId = new SessionManager(this).getUserId();
        loadProfile();

        // Save Changes
        btnSaveChanges.setOnClickListener(v -> saveProfile());
    }

    /** Fills the form with the logged-in account (same pattern as CustomerProfileActivity). */
    private void loadProfile() {
        AuthTasks.run(this, null, () -> AppDatabaseHelper.getInstance(this).getUser(userId), user -> {
            if (user == null) {
                AuthNavigator.logout(this);
                return;
            }
            inputFullName.setText(user.fullName);
            etAddress.setText(emptyIfNull(user.address));
            etCity.setText(user.city);
            etProvince.setText(user.province);
            etPostalCode.setText(user.postalCode);
            etPhone.setText(emptyIfNull(user.phone));
        });
    }

    private void saveProfile() {
        String fullName = textOf(inputFullName);
        String address = textOf(etAddress);
        String city = textOf(etCity);
        String province = textOf(etProvince);
        String postalCode = textOf(etPostalCode);
        String phone = textOf(etPhone);

        // Name, city, province and postal code are required (same as sign up)
        if (fullName.isEmpty()) {
            inputFullName.setError(getString(R.string.msgFullNameRequired));
            return;
        }
        if (city.isEmpty() || province.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(this, R.string.msgCompleteAllFields, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthTasks.run(this, btnSaveChanges,
                () -> AppDatabaseHelper.getInstance(this).updateProfile(
                        userId, fullName, address, city, province, postalCode, phone),
                user -> {
                    Toast.makeText(this, R.string.msgProfileSaved, Toast.LENGTH_SHORT).show();
                    finish(); // back to Profile, which reloads in onResume()
                });
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private String textOf(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}