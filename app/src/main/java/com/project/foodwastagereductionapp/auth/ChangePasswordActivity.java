package com.project.foodwastagereductionapp.auth;

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

/** Available to both roles; a current password is always required. */
public class ChangePasswordActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right,
                    Math.max(bars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom));
            return insets;
        });
        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.btnChangePassword);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        SessionManager session = new SessionManager(this);
        long userId = session.getUserId();
        if (userId < 0) { AuthNavigator.logout(this); return; }
        EditText current = findViewById(R.id.editCurrentPassword);
        EditText password = findViewById(R.id.editNewPassword);
        EditText confirmation = findViewById(R.id.editConfirmNewPassword);
        Button save = findViewById(R.id.btnChangePassword);
        save.setEnabled(false);
        AuthTasks.run(this, null, () -> AppDatabaseHelper.getInstance(this).getUser(userId), user -> {
            if (user == null || !user.emailVerified) { AuthNavigator.logout(this); return; }
            save.setEnabled(true);
        });
        save.setOnClickListener(v -> {
            String oldValue = current.getText().toString();
            String value = password.getText().toString();
            if (oldValue.isEmpty()) { current.setError(getString(R.string.msgCurrentPasswordRequired)); return; }
            if (value.length() < 6) { password.setError(getString(R.string.msgPasswordTooShort)); return; }
            if (!value.equals(confirmation.getText().toString())) {
                confirmation.setError(getString(R.string.msgPasswordsDoNotMatch)); return;
            }
            AuthTasks.run(this, save, () -> AppDatabaseHelper.getInstance(this)
                    .changePassword(userId, oldValue, value), updated -> {
                if (!updated) { current.setError(getString(R.string.msgIncorrectPassword)); return; }
                Toast.makeText(this, R.string.msgPasswordChangedLogin, Toast.LENGTH_LONG).show();
                AuthNavigator.logout(this);
            });
        });
    }
}
