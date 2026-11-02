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

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

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

        // Input fields
        EditText editNewPassword =
                findViewById(R.id.editNewPassword);

        EditText editConfirmNewPassword =
                findViewById(R.id.editConfirmNewPassword);

        // Reset button
        Button btnResetPassword =
                findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(v -> {

            String newPassword =
                    editNewPassword.getText().toString().trim();

            String confirmPassword =
                    editConfirmNewPassword.getText().toString().trim();

            // Empty validation
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {

                Toast.makeText(
                        ResetPasswordActivity.this,
                        "Please enter both password fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Password match validation
            if (!newPassword.equals(confirmPassword)) {

                Toast.makeText(
                        ResetPasswordActivity.this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Actual password update logic will be added later

            // Go to Password Reset Complete Page
            Intent intent = new Intent(
                    ResetPasswordActivity.this,
                    PasswordResetCompleteActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}