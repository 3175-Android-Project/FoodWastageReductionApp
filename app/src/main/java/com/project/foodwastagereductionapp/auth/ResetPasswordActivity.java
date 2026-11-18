package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.ui.BaseActivity;

public class ResetPasswordActivity extends BaseActivity {
    public static final String EXTRA_RESET_TOKEN = "resetToken";
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right,
                    Math.max(bars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom));
            return insets;
        });
        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.app_name);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        String email = AppDatabaseHelper.normalizeEmail(getIntent().getStringExtra("email"));
        String token = getIntent().getStringExtra(EXTRA_RESET_TOKEN);
        EditText password = findViewById(R.id.editNewPassword);
        EditText confirmation = findViewById(R.id.editConfirmNewPassword);
        Button save = findViewById(R.id.btnResetPassword);
        save.setEnabled(false);
        AuthTasks.run(this, null, () -> AppDatabaseHelper.getInstance(this)
                .isPasswordResetGrantValid(email, token), valid -> {
            if (!valid) { authorizationExpired(); return; }
            save.setEnabled(true);
        });
        save.setOnClickListener(v -> {
            String value = password.getText().toString();
            if (value.length() < 6) { password.setError(getString(R.string.msgPasswordTooShort)); return; }
            if (!value.equals(confirmation.getText().toString())) {
                confirmation.setError(getString(R.string.msgPasswordsDoNotMatch)); return;
            }
            AuthTasks.run(this, save, () -> AppDatabaseHelper.getInstance(this)
                    .resetPassword(email, token, value), updated -> {
                if (!updated) { authorizationExpired(); return; }
                new SessionManager(this).clear();
                Intent login = new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivities(new Intent[]{login, new Intent(this, PasswordResetCompleteActivity.class)});
                finish();
            });
        });
    }
    private void authorizationExpired() {
        Toast.makeText(this, R.string.msgResetAuthorizationExpired, Toast.LENGTH_LONG).show();
        finish();
    }
}
