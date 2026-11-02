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

        String selectedRole =
                getIntent().getStringExtra("selectedRole");

        String email =
                getIntent().getStringExtra("email");

        String password =
                getIntent().getStringExtra("password");

        String fullName =
                getIntent().getStringExtra("fullName");

        String city =
                getIntent().getStringExtra("city");

        String province =
                getIntent().getStringExtra("province");

        String postalCode =
                getIntent().getStringExtra("postalCode");

        if (email == null) {
            email = "";
        }

        // Email message
        TextView txtVerificationMessage =
                findViewById(R.id.txtVerificationMessage);

        txtVerificationMessage.setText(
                getString(
                        R.string.emailVerificationMessage,
                        email
                )
        );

        Button btnContinue =
                findViewById(R.id.btnVerificationContinue);

        /*
         * Customer → Login
         * Manager → Restaurant Information
         */
        if ("manager".equals(selectedRole)) {
            btnContinue.setText(R.string.btnContinue);
        } else {
            btnContinue.setText(R.string.btnGoToLogin);
        }

        String finalEmail = email;

        btnContinue.setOnClickListener(v -> {

            if ("manager".equals(selectedRole)) {

                Intent intent = new Intent(
                        EmailVerificationActivity.this,
                        RestaurantInformationActivity.class
                );

                intent.putExtra("email", finalEmail);
                intent.putExtra("password", password);
                intent.putExtra("fullName", fullName);

                intent.putExtra("city", city);
                intent.putExtra("province", province);
                intent.putExtra("postalCode", postalCode);

                startActivity(intent);

            } else {

                Intent intent = new Intent(
                        EmailVerificationActivity.this,
                        LoginActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(intent);
                finish();
            }
        });

        Button btnResendVerification =
                findViewById(R.id.btnResendVerification);

        btnResendVerification.setOnClickListener(v -> {
            // Actual verification email logic will be added later
        });
    }
}