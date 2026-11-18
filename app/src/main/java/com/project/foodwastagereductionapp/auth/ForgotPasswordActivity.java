package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.project.foodwastagereductionapp.BuildConfig;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.ui.BaseActivity;

public class ForgotPasswordActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right,
                    Math.max(bars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom));
            return insets;
        });
        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.app_name);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.txtBackToLogin).setOnClickListener(v -> finish());
        EditText input = findViewById(R.id.editEmail);
        Button send = findViewById(R.id.btnSendResetLink);
        send.setOnClickListener(v -> {
            String email = AppDatabaseHelper.normalizeEmail(input.getText().toString());
            if (!AppDatabaseHelper.validEmail(email)) {
                input.setError(getString(R.string.msgInvalidEmail));
                return;
            }
            send.setText(R.string.txtSendingCode);
            AuthTasks.run(this, send, () -> PasswordResetVerification.request(
                    AppDatabaseHelper.getInstance(this),
                    new EmailVerificationClient(BuildConfig.EMAIL_VERIFICATION_URL), email), challenge -> {
                send.setText(R.string.btnSendResetCode);
                startActivity(new Intent(this, ResetLinkSentActivity.class)
                        .putExtra("email", email).putExtra("challengeId", challenge.id)
                        .putExtra("expiresIn", challenge.expiresIn)
                        .putExtra("retryAfter", challenge.retryAfter));
            }, error -> {
                send.setText(R.string.btnSendResetCode);
                input.setError(ResetErrors.message(this, error));
            });
        });
    }
}
