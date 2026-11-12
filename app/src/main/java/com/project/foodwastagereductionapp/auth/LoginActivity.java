package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        int screenPadding =
                getResources().getDimensionPixelSize(R.dimen.screen_padding);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left + screenPadding,
                    systemBars.top,
                    systemBars.right + screenPadding,
                    systemBars.bottom
            );

            return insets;
        });

        // Header title
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.app_name);

        // Back
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Entering Login ends any previous account session.
        SessionManager session = new SessionManager(this);
        session.clear();
        EditText editEmail = findViewById(R.id.editEmail);
        EditText editPassword = findViewById(R.id.editPassword);
        if (savedInstanceState == null && getIntent().hasExtra("email")) {
            editEmail.setText(getIntent().getStringExtra("email"));
        }
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            String email = AppDatabaseHelper.normalizeEmail(editEmail.getText().toString());
            String password = editPassword.getText().toString();
            if (!AppDatabaseHelper.validEmail(email)) {
                editEmail.setError(getString(R.string.msgInvalidEmail));
                return;
            }
            if (password.isEmpty()) {
                editPassword.setError(getString(R.string.msgCompleteAllFields));
                return;
            }
            AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
            AuthTasks.run(this, btnLogin, () -> {
                User user = db.authenticate(email, password);
                return user == null ? null : new LoginResult(user, AuthNavigator.destination(db, user));
            }, result -> {
                if (result == null) {
                    editPassword.setError(getString(R.string.msgInvalidCredentials));
                    return;
                }
                if (result.user.emailVerified) session.start(result.user.id);
                editPassword.setText("");
                AuthNavigator.open(this, result.screen, result.user.id);
            });
        });

        // Forgot Password
        TextView txtForgotPassword = findViewById(R.id.txtForgotPassword);

        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    ForgotPasswordActivity.class
            );

            startActivity(intent);
        });

        // Create account
        TextView txtCreateAccount = findViewById(R.id.txtCreateAccount);

        txtCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
    private static final class LoginResult {
        final User user;
        final Class<?> screen;
        LoginResult(User user, Class<?> screen) { this.user = user; this.screen = screen; }
    }
}
