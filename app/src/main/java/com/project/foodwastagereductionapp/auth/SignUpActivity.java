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

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

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

        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.app_name);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        EditText editEmail = findViewById(R.id.editEmail);
        EditText editPassword = findViewById(R.id.editPassword);
        EditText editConfirmPassword = findViewById(R.id.editConfirmPassword);

        Button btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {

            String email =
                    editEmail.getText().toString().trim();

            String password =
                    editPassword.getText().toString();

            String confirmPassword =
                    editConfirmPassword.getText().toString();

            if (email.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                Toast.makeText(
                        SignUpActivity.this,
                        R.string.msgCompleteAllFields,
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!password.equals(confirmPassword)) {

                Toast.makeText(
                        SignUpActivity.this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    SignUpActivity.this,
                    ChooseRoleActivity.class
            );

            intent.putExtra("email", email);

            // Temporary until SQLite is implemented
            intent.putExtra("password", password);

            startActivity(intent);
        });

        TextView txtLogin = findViewById(R.id.txtLogin);

        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SignUpActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}