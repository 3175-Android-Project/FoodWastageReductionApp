package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;

public class ResetLinkSentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_link_sent);

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

        String email = getIntent().getStringExtra("email");

        if (email == null || email.isEmpty()) {
            email = "aaa@gmail.com";
        }

        TextView txtMessage = findViewById(R.id.txtResetLinkSentMessage);
        txtMessage.setText(
                getString(R.string.resetLinkSentMessage, email)
        );

        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ResetLinkSentActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            finish();
        });

        // temporary
        Button btnResendLink = findViewById(R.id.btnResendLink);

        btnResendLink.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ResetLinkSentActivity.this,
                    ResetPasswordActivity.class
            );

            startActivity(intent);
        });
    }
}