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

/** Edits the signed-in user's account (saved in the database). */
public class EditProfileActivity extends BaseActivity {

    private EditText inputFullName, etAddress, etCity, etProvince, etPostalCode, etPhone;
    private Button btnSaveChanges;
    private long userId;
    private boolean restoringForm;

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

        etCity.setFocusable(false);
        etCity.setClickable(true);
        etCity.setOnClickListener(v -> com.project.foodwastagereductionapp.ui.CityPickerBottomSheet.show(this, selectedCity -> {
            etCity.setText(selectedCity);
            etCity.setError(null);
        }));

        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        btnSaveChanges.setEnabled(false);
        restoringForm = savedInstanceState != null;
        userId = new SessionManager(this).getUserId();
        loadProfile();

        // Save Changes
        btnSaveChanges.setOnClickListener(v -> saveProfile());
    }

    /** Fills the form with the logged-in account (same pattern as CustomerProfileActivity). */
    private void loadProfile() {
        AuthTasks.run(this, null, () -> AppDatabaseHelper.getInstance(this).getUser(userId), user -> {
            if (user == null || !user.emailVerified) {
                AuthNavigator.logout(this);
                return;
            }
            if (!restoringForm) {
                inputFullName.setText(user.fullName);
                etAddress.setText(emptyIfNull(user.address));
                etCity.setText(user.city);
                etProvince.setText(user.province);
                etPostalCode.setText(user.postalCode);
                etPhone.setText(emptyIfNull(user.phone));
            }
            btnSaveChanges.setEnabled(true);
        });
    }

    private void saveProfile() {
        String fullName = textOf(inputFullName);
        String address = textOf(etAddress);
        String city = textOf(etCity);
        String province = textOf(etProvince);
        String postalCode = textOf(etPostalCode);
        String phone = textOf(etPhone);

        if (fullName.isEmpty()) {
            inputFullName.setError(getString(R.string.msgFullNameRequired));
            return;
        }
        if (city.isEmpty()) {
            Toast.makeText(this, R.string.msgCompleteAllFields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) address = city + ", BC";
        if (province.isEmpty()) province = "BC";
        if (postalCode.isEmpty()) postalCode = "V6B 1A1";

        String finalAddress = address;
        String finalProvince = province;
        String finalPostalCode = postalCode;

        AuthTasks.run(this, btnSaveChanges,
                () -> AppDatabaseHelper.getInstance(this).updateProfile(
                        userId, fullName, finalAddress, city, finalProvince, finalPostalCode, phone),
                user -> {
                    SessionManager.invalidateCaches();
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
