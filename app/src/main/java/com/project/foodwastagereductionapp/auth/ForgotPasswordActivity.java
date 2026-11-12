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

import com.project.foodwastagereductionapp.BuildConfig;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button btnSendResetLink;
    private EditText editEmail;
    private boolean sending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

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
        TextView txtHeaderTitle =
                findViewById(R.id.txtHeaderTitle);

        txtHeaderTitle.setText(R.string.app_name);


        // Back button
        ImageView btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        TextView txtBackToLogin =
                findViewById(R.id.txtBackToLogin);

        txtBackToLogin.setOnClickListener(v -> finish());


        // Input / Button
        editEmail =
                findViewById(R.id.editEmail);

        btnSendResetLink =
                findViewById(R.id.btnSendResetLink);


        btnSendResetLink.setOnClickListener(v -> {

            if (sending) {
                return;
            }

            String email =
                    editEmail.getText()
                            .toString()
                            .trim();

            // Empty validation
            if (email.isEmpty()) {

                editEmail.setError(
                        "Please enter your email"
                );

                editEmail.requestFocus();

                return;
            }

            // Email format validation
            if (!AppDatabaseHelper.validEmail(email)) {

                editEmail.setError(
                        "Please enter a valid email"
                );

                editEmail.requestFocus();

                return;
            }

            sendResetCode(email);
        });
    }


    private void sendResetCode(String email) {
        sending = true;

        btnSendResetLink.setEnabled(false);
        btnSendResetLink.setText("Sending...");


        EmailVerificationClient client =
                new EmailVerificationClient(
                        BuildConfig.EMAIL_VERIFICATION_URL
                );


        AuthTasks.run(
                this,
                null,

                // Background thread
                () -> client.requestCode(email),

                // Success
                challenge -> {

                    sending = false;

                    btnSendResetLink.setEnabled(true);
                    btnSendResetLink.setText(
                            R.string.btnSendResetCode
                    );


                    Intent intent =
                            new Intent(
                                    ForgotPasswordActivity.this,
                                    ResetLinkSentActivity.class
                            );


                    // Email used for verification
                    intent.putExtra(
                            "email",
                            email
                    );


                    // Needed when verifying the code
                    intent.putExtra(
                            "challengeId",
                            challenge.id
                    );


                    // Optional values for resend timer
                    intent.putExtra(
                            "expiresIn",
                            challenge.expiresIn
                    );

                    intent.putExtra(
                            "retryAfter",
                            challenge.retryAfter
                    );


                    startActivity(intent);
                },

                // Error
                error -> {

                    sending = false;

                    btnSendResetLink.setEnabled(true);
                    btnSendResetLink.setText(
                            R.string.btnSendResetCode
                    );


                    String message =
                            "Unable to send verification code. Please try again.";


                    if (error instanceof EmailVerificationClient.RequestException) {

                        EmailVerificationClient.RequestException requestError =
                                (EmailVerificationClient.RequestException) error;


                        if ("too_many_requests".equals(
                                requestError.reason
                        )) {

                            message =
                                    "Please wait before requesting another code.";

                        } else if ("invalid_email".equals(
                                requestError.reason
                        )) {

                            message =
                                    "Please enter a valid email address.";

                        } else if ("delivery_failed".equals(
                                requestError.reason
                        )) {

                            message =
                                    "Unable to deliver the email.";
                        }
                    }


                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }
}