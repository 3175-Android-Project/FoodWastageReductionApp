package com.project.foodwastagereductionapp.auth;

import com.project.foodwastagereductionapp.data.AppDatabaseHelper;

/** The proof can only be created after a successful email-service response. */
public final class PasswordResetVerification {
    private PasswordResetVerification() { }

    public static final class VerifiedEmail {
        private final String email;
        private VerifiedEmail(String email) { this.email = email; }
        public String getEmail() { return email; }
    }

    static EmailVerificationClient.Challenge request(AppDatabaseHelper db,
                                                     EmailVerificationClient client, String email)
            throws Exception {
        String normalized = AppDatabaseHelper.normalizeEmail(email);
        if (!db.emailExists(normalized)) throw new AccountNotFoundException();
        return client.requestCode(normalized);
    }

    static String verifyAndAuthorize(AppDatabaseHelper db, EmailVerificationClient client,
                                     String email, String challengeId, String code) throws Exception {
        String normalized = AppDatabaseHelper.normalizeEmail(email);
        if (!db.emailExists(normalized)) throw new AccountNotFoundException();
        client.verifyCode(normalized, challengeId, code);
        return db.issuePasswordResetGrant(new VerifiedEmail(normalized));
    }

    static final class AccountNotFoundException extends IllegalArgumentException { }
}
