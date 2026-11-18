package com.project.foodwastagereductionapp.auth;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.project.foodwastagereductionapp.BuildConfig;
import com.project.foodwastagereductionapp.data.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.UUID;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class PasswordResetVerificationTest {
    private Context context; private AppDatabaseHelper db; private String email; private EmailVerificationClient client;
    @Before public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase("reset-service-test.db");
        db = new AppDatabaseHelper(context, "reset-service-test.db");
        email = "reset-" + UUID.randomUUID() + "@example.com";
        db.createUser(email, PasswordHasher.hash("Original123"), User.CUSTOMER,"Test","Vancouver","BC","V6B1A1");
        client = new EmailVerificationClient(BuildConfig.EMAIL_VERIFICATION_URL);
    }
    @After public void cleanup() { db.close(); context.deleteDatabase("reset-service-test.db"); }
    @Test public void missingAccountIsRejectedBeforeNetworkRequest() throws Exception {
        try {
            PasswordResetVerification.request(db, new EmailVerificationClient("invalid-url"), "missing@example.com");
            fail("Unknown email must not reach the mail service");
        } catch (PasswordResetVerification.AccountNotFoundException expected) { }
    }
    @Test public void uppercaseCodeVerificationCreatesBoundOneTimeAuthorization() throws Exception {
        localServerOnly();
        EmailVerificationClient.Challenge challenge = PasswordResetVerification.request(db, client, "  " + email.toUpperCase(java.util.Locale.ROOT) + "  ");
        try { PasswordResetVerification.verifyAndAuthorize(db,client,email,challenge.id,"000000"); fail(); }
        catch (EmailVerificationClient.RequestException expected) { assertEquals("invalid_code",expected.reason); }
        assertNotNull(db.authenticate(email,"Original123"));
        String token = PasswordResetVerification.verifyAndAuthorize(db,client,email.toUpperCase(java.util.Locale.ROOT),challenge.id,"123456");
        assertTrue(db.isPasswordResetGrantValid(email,token));
        try { PasswordResetVerification.verifyAndAuthorize(db,client,email,challenge.id,"123456"); fail(); }
        catch (EmailVerificationClient.RequestException expected) { assertEquals("invalid_challenge",expected.reason); }
        assertTrue(db.resetPassword(email,token,"Updated123"));
        assertFalse(db.resetPassword(email,token,"Another123"));
        assertNotNull(db.authenticate(email,"Updated123"));
    }
    private void localServerOnly() {
        assumeTrue("Run with the disposable local email server", BuildConfig.EMAIL_VERIFICATION_URL.equals("http://10.0.2.2:8766")
                && "123456".equals(InstrumentationRegistry.getArguments().getString("verificationTestCode")));
    }
}
