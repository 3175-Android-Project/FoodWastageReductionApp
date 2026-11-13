package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.BuildConfig;
import com.project.foodwastagereductionapp.R;

public class ResetLinkSentActivity extends AppCompatActivity {

    private EditText editResetCode;
    private Button btnVerifyResetCode;
    private Button btnResendCode;

    private String email;
    private String challengeId;

    private boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_link_sent);

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

        // Get data from ForgotPasswordActivity
        email =
                getIntent().getStringExtra("email");

        challengeId =
                getIntent().getStringExtra("challengeId");


        // Invalid navigation
        if (email == null
                || email.isEmpty()
                || challengeId == null
                || challengeId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Unable to verify reset code.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // Message
        TextView txtMessage = findViewById(R.id.txtResetLinkSentMessage);

        txtMessage.setText(
                getString(
                        R.string.resetLinkSentMessage,
                        email
                )
        );


        // Input
        editResetCode =
                findViewById(
                        R.id.editResetCode
                );


        // Verify button
        btnVerifyResetCode =
                findViewById(
                        R.id.btnVerifyResetCode
                );


        // Resend button
        btnResendCode =
                findViewById(
                        R.id.btnResendLink
                );


        // Back to Login
        TextView txtBackToLogin =
                findViewById(
                        R.id.txtBackToLogin
                );

        txtBackToLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
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


        /*
         * Verify reset code
         */
        btnVerifyResetCode.setOnClickListener(v -> {

            if (busy) {
                return;
            }

            String code =
                    editResetCode
                            .getText()
                            .toString()
                            .trim();


            if (!code.matches("[0-9]{6}")) {

                editResetCode.setError(
                        "Enter the 6-digit code"
                );

                editResetCode.requestFocus();

                return;
            }


            verifyCode(code);
        });


        /*
         * Resend reset code
         */
        btnResendCode.setOnClickListener(v -> {

            if (busy) {
                return;
            }

            resendCode();
        });
    }


    private void verifyCode(String code) {

        if (BuildConfig.EMAIL_VERIFICATION_URL == null
                || BuildConfig.EMAIL_VERIFICATION_URL.isEmpty()) {

            Toast.makeText(
                    this,
                    "Email server is not configured.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        busy = true;

        btnVerifyResetCode.setEnabled(false);
        btnResendCode.setEnabled(false);

        btnVerifyResetCode.setText("Verifying...");


        EmailVerificationClient client =
                new EmailVerificationClient(
                        BuildConfig.EMAIL_VERIFICATION_URL
                );


        AuthTasks.run(
                this,
                null,

                // Background
                () -> {

                    client.verifyCode(
                            email,
                            challengeId,
                            code
                    );

                    return true;
                },

                // Success
                success -> {

                    busy = false;

                    btnVerifyResetCode.setEnabled(true);
                    btnResendCode.setEnabled(true);

                    btnVerifyResetCode.setText(
                            R.string.btnVerifyEmailCode
                    );


                    Intent intent =
                            new Intent(
                                    ResetLinkSentActivity.this,
                                    ResetPasswordActivity.class
                            );


                    /*
                     * ResetPasswordActivity will need this
                     * to update the correct Room account.
                     */
                    intent.putExtra(
                            "email",
                            email
                    );


                    startActivity(intent);

                    finish();
                },

                // Error
                error -> {

                    busy = false;

                    btnVerifyResetCode.setEnabled(true);
                    btnResendCode.setEnabled(true);

                    btnVerifyResetCode.setText(
                            R.string.btnVerifyEmailCode
                    );


                    String message =
                            "Unable to verify code.";


                    if (error instanceof EmailVerificationClient.RequestException) {

                        EmailVerificationClient.RequestException requestError =
                                (EmailVerificationClient.RequestException) error;


                        switch (requestError.reason) {

                            case "invalid_code":

                                editResetCode.setError(
                                        "Incorrect verification code"
                                );

                                return;


                            case "expired_code":

                                message =
                                        "This code has expired. Please request a new code.";

                                break;


                            case "attempts_exhausted":

                                message =
                                        "Too many incorrect attempts. Please request a new code.";

                                break;


                            case "invalid_challenge":

                                message =
                                        "This code is no longer valid. Please request a new code.";

                                break;


                            default:

                                message =
                                        "Unable to verify code. Please try again.";

                                break;
                        }
                    }


                    Toast.makeText(
                            ResetLinkSentActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }


    private void resendCode() {

        if (BuildConfig.EMAIL_VERIFICATION_URL == null
                || BuildConfig.EMAIL_VERIFICATION_URL.isEmpty()) {

            Toast.makeText(
                    this,
                    "Email server is not configured.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        busy = true;

        btnVerifyResetCode.setEnabled(false);
        btnResendCode.setEnabled(false);

        btnResendCode.setText(
                "Sending..."
        );


        EmailVerificationClient client =
                new EmailVerificationClient(
                        BuildConfig.EMAIL_VERIFICATION_URL
                );


        AuthTasks.run(
                this,
                null,

                // Background
                () ->
                        client.requestCode(email),

                // Success
                challenge -> {

                    /*
                     * VERY IMPORTANT:
                     * use the NEW challenge id.
                     */
                    challengeId =
                            challenge.id;


                    busy = false;

                    btnVerifyResetCode.setEnabled(true);
                    btnResendCode.setEnabled(true);

                    btnResendCode.setText(
                            R.string.btnResendCode
                    );


                    editResetCode.setText("");
                    editResetCode.setError(null);


                    Toast.makeText(
                            ResetLinkSentActivity.this,
                            "A new reset code was sent.",
                            Toast.LENGTH_SHORT
                    ).show();
                },

                // Error
                error -> {

                    busy = false;

                    btnVerifyResetCode.setEnabled(true);
                    btnResendCode.setEnabled(true);

                    btnResendCode.setText(
                            R.string.btnResendCode
                    );


                    String message =
                            "Unable to resend code.";


                    if (error instanceof EmailVerificationClient.RequestException) {

                        EmailVerificationClient.RequestException requestError =
                                (EmailVerificationClient.RequestException) error;


                        if ("too_many_requests".equals(
                                requestError.reason
                        )) {

                            message =
                                    "Please wait before requesting another code.";
                        }
                    }


                    Toast.makeText(
                            ResetLinkSentActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }
}