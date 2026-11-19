package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.ui.BaseActivity;

public class AccountInformationActivity extends BaseActivity {
    private com.project.foodwastagereductionapp.profile.ProfilePhotoPicker photoPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_information);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        String selectedRole =
                getIntent().getStringExtra("selectedRole");

        String email =
                getIntent().getStringExtra("email");

        String passwordHash = getIntent().getStringExtra(AuthNavigator.EXTRA_PASSWORD_HASH);
        if (!AppDatabaseHelper.validEmail(email) || passwordHash == null
                || !(User.CUSTOMER.equals(selectedRole) || User.MANAGER.equals(selectedRole))) {
            AuthNavigator.open(this, SignUpActivity.class, -1);
            return;
        }

        // Header
        TextView txtHeaderTitle =
                findViewById(R.id.txtHeaderTitle);

        txtHeaderTitle.setText(R.string.app_name);

        ImageView btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        photoPicker = new com.project.foodwastagereductionapp.profile.ProfilePhotoPicker(this, savedInstanceState);
        EditText editAddress = findViewById(R.id.editAddress);

        // Inputs
        EditText editFullName =
                findViewById(R.id.editFullName);

        EditText editCity =
                findViewById(R.id.editCity);

        EditText editProvince =
                findViewById(R.id.editProvince);

        EditText editPostalCode =
                findViewById(R.id.editPostalCode);

        Button btnCreateAccount =
                findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v -> {

            if (photoPicker.isBusy()) return;
            String address = editAddress.getText().toString().trim();
            if (address.isEmpty()) { editAddress.setError(getString(R.string.msgAddressRequired)); return; }
            String photoUri = photoPicker.getPhotoUri();
            String fullName =
                    editFullName.getText().toString().trim();

            String city =
                    editCity.getText().toString().trim();

            String province =
                    editProvince.getText().toString().trim();

            String postalCode =
                    editPostalCode.getText().toString().trim();

            if (fullName.isEmpty()
                    || city.isEmpty()
                    || province.isEmpty()
                    || postalCode.isEmpty()) {

                Toast.makeText(
                        AccountInformationActivity.this,
                        R.string.msgCompleteAllFields,
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
            AuthTasks.run(this, btnCreateAccount,
                    () -> db.createUser(email, passwordHash, selectedRole, fullName,
                            city, province, postalCode, address, photoUri),
                    userId -> AuthNavigator.open(this, EmailVerificationActivity.class, userId));
        });
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (photoPicker != null) photoPicker.saveState(state);
        super.onSaveInstanceState(state);
    }
}
