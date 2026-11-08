package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class EmailVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email_verification);

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

        long userId = getIntent().getLongExtra(AuthNavigator.EXTRA_USER_ID, -1);
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        Button btnContinue = findViewById(R.id.btnVerificationContinue);
        btnContinue.setEnabled(false);
        AuthTasks.run(this, null, () -> db.getUser(userId), user -> {
            if (user == null) {
                AuthNavigator.logout(this);
                return;
            }
            TextView message = findViewById(R.id.txtVerificationMessage);
            message.setText(getString(R.string.emailVerificationMessage, user.email));
            btnContinue.setText(User.MANAGER.equals(user.role)
                    ? R.string.btnLocalVerifyContinue : R.string.btnLocalVerifyLogin);
            btnContinue.setEnabled(true);
        });
        btnContinue.setOnClickListener(v -> AuthTasks.run(this, btnContinue,
                () -> db.confirmEmailLocally(userId), user -> {
                    if (User.MANAGER.equals(user.role)) {
                        new SessionManager(this).start(user.id);
                        AuthNavigator.open(this, RestaurantInformationActivity.class, user.id);
                    } else {
                        new SessionManager(this).clear();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.putExtra("email", user.email);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }));
        findViewById(R.id.btnResendVerification).setOnClickListener(v ->
                Toast.makeText(this, R.string.msgLocalVerification, Toast.LENGTH_LONG).show());
    }
}
