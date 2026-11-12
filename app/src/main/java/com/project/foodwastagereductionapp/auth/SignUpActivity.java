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
import com.project.foodwastagereductionapp.data.PasswordHasher;
import com.project.foodwastagereductionapp.data.User;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

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

        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.app_name);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        EditText editEmail = findViewById(R.id.editEmail);
        EditText editPassword = findViewById(R.id.editPassword);
        EditText editConfirmPassword = findViewById(R.id.editConfirmPassword);

        Button btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {

            String email = AppDatabaseHelper.normalizeEmail(editEmail.getText().toString());

            String password =
                    editPassword.getText().toString();

            String confirmPassword =
                    editConfirmPassword.getText().toString();

            if (email.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                Toast.makeText(
                        SignUpActivity.this,
                        R.string.msgCompleteAllFields,
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!password.equals(confirmPassword)) {

                Toast.makeText(
                        SignUpActivity.this,
                        R.string.msgPasswordsDoNotMatch,
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!AppDatabaseHelper.validEmail(email)) {
                editEmail.setError(getString(R.string.msgInvalidEmail));
                return;
            }
            if (password.length() < 6) {
                editPassword.setError(getString(R.string.msgPasswordTooShort));
                return;
            }
            AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
            AuthTasks.run(this, btnContinue, () -> {
                if (db.emailExists(email)) {
                    User existing = db.authenticate(email, password);
                    if (existing == null || existing.emailVerified) {
                        throw new AppDatabaseHelper.DuplicateEmailException();
                    }
                    return new Intent(this, EmailVerificationActivity.class)
                            .putExtra(AuthNavigator.EXTRA_USER_ID, existing.id);
                }
                return new Intent(this, ChooseRoleActivity.class)
                        .putExtra("email", email)
                        .putExtra(AuthNavigator.EXTRA_PASSWORD_HASH, PasswordHasher.hash(password));
            }, intent -> {
                long pendingUserId = intent.getLongExtra(AuthNavigator.EXTRA_USER_ID, -1);
                if (pendingUserId != -1) {
                    AuthNavigator.open(this, EmailVerificationActivity.class, pendingUserId);
                } else {
                    startActivity(intent);
                }
            });
        });

        TextView txtLogin = findViewById(R.id.txtLogin);

        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SignUpActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}
