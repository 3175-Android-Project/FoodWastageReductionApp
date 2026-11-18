package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.project.foodwastagereductionapp.BuildConfig;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.ui.BaseActivity;

public class ResetLinkSentActivity extends BaseActivity {
    private String email, challengeId;
    private EditText codeInput;
    private Button verify, resend;
    private boolean busy, active;
    private long resendAt;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateTimer = () -> updateControls();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_link_sent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right,
                    Math.max(bars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom));
            return insets;
        });
        email = AppDatabaseHelper.normalizeEmail(getIntent().getStringExtra("email"));
        challengeId = state == null ? getIntent().getStringExtra("challengeId") : state.getString("challengeId");
        resendAt = state == null ? SystemClock.elapsedRealtime()
                + getIntent().getIntExtra("retryAfter", 60) * 1000L : state.getLong("resendAt");
        if (!AppDatabaseHelper.validEmail(email) || challengeId == null) { finish(); return; }
        ((TextView) findViewById(R.id.txtResetLinkSentMessage))
                .setText(getString(R.string.resetLinkSentMessage, email));
        codeInput = findViewById(R.id.editResetCode);
        verify = findViewById(R.id.btnVerifyResetCode);
        resend = findViewById(R.id.btnResendLink);
        findViewById(R.id.txtBackToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        verify.setOnClickListener(v -> verifyCode());
        resend.setOnClickListener(v -> resendCode());
        updateControls();
    }

    private void verifyCode() {
        if (busy) return;
        String code = codeInput.getText().toString().trim();
        if (!code.matches("[0-9]{6}")) {
            codeInput.setError(getString(R.string.msgEnterEmailCode));
            return;
        }
        busy = true;
        updateControls();
        AuthTasks.run(this, null, () -> PasswordResetVerification.verifyAndAuthorize(
                AppDatabaseHelper.getInstance(this),
                new EmailVerificationClient(BuildConfig.EMAIL_VERIFICATION_URL), email, challengeId, code), token -> {
            startActivity(new Intent(this, ResetPasswordActivity.class)
                    .putExtra("email", email).putExtra(ResetPasswordActivity.EXTRA_RESET_TOKEN, token));
            finish();
        }, error -> {
            busy = false;
            codeInput.setError(ResetErrors.message(this, error));
            updateControls();
        });
    }

    private void resendCode() {
        if (busy || SystemClock.elapsedRealtime() < resendAt) return;
        busy = true;
        updateControls();
        AuthTasks.run(this, null, () -> PasswordResetVerification.request(
                AppDatabaseHelper.getInstance(this),
                new EmailVerificationClient(BuildConfig.EMAIL_VERIFICATION_URL), email), challenge -> {
            challengeId = challenge.id;
            resendAt = SystemClock.elapsedRealtime() + challenge.retryAfter * 1000L;
            codeInput.setText("");
            codeInput.setError(null);
            busy = false;
            updateControls();
            Toast.makeText(this, R.string.msgResetCodeSent, Toast.LENGTH_SHORT).show();
        }, error -> {
            busy = false;
            codeInput.setError(ResetErrors.message(this, error));
            updateControls();
        });
    }

    private void updateControls() {
        if (verify == null) return;
        verify.setEnabled(!busy);
        resend.setEnabled(!busy && SystemClock.elapsedRealtime() >= resendAt);
        handler.removeCallbacks(updateTimer);
        if (active && !resend.isEnabled()) handler.postDelayed(updateTimer, 1000);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("challengeId", challengeId);
        state.putLong("resendAt", resendAt);
        super.onSaveInstanceState(state);
    }
    @Override protected void onStop() { active = false; super.onStop(); handler.removeCallbacks(updateTimer); }
    @Override protected void onStart() { super.onStart(); active = true; updateControls(); }
}
