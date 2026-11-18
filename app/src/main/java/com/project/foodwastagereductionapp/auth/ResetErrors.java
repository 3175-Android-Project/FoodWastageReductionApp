package com.project.foodwastagereductionapp.auth;

import android.content.Context;
import com.project.foodwastagereductionapp.R;

final class ResetErrors {
    private ResetErrors() { }
    static String message(Context context, Exception error) {
        int message = R.string.msgResetRequestFailed;
        if (error instanceof PasswordResetVerification.AccountNotFoundException) {
            message = R.string.msgAccountNotFound;
        } else if (error instanceof EmailVerificationClient.RequestException) {
            switch (((EmailVerificationClient.RequestException) error).reason) {
                case "invalid_code": message = R.string.msgIncorrectEmailCode; break;
                case "expired_code": message = R.string.msgExpiredEmailCode; break;
                case "invalid_challenge": message = R.string.msgResetAuthorizationExpired; break;
                case "attempts_exhausted": message = R.string.msgResetAttemptsExhausted; break;
                case "too_many_requests": message = R.string.msgResetWait; break;
            }
        }
        return context.getString(message);
    }
}
