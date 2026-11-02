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

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

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

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.app_name);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Back to Login
        TextView txtBackToLogin = findViewById(R.id.txtBackToLogin);
        txtBackToLogin.setOnClickListener(v -> finish());

        // Reset Link
        Button btnSendResetLink = findViewById(R.id.btnSendResetLink);

        EditText editEmail = findViewById(R.id.editEmail);

        btnSendResetLink.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            Intent intent = new Intent(
                    ForgotPasswordActivity.this,
                    ResetLinkSentActivity.class
            );

            intent.putExtra("email", email);

            startActivity(intent);
        });
    }
}