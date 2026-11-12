package com.project.foodwastagereductionapp;

import android.app.Activity;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.project.foodwastagereductionapp.auth.*;
import com.project.foodwastagereductionapp.customer.CustomerHomeActivity;
import com.project.foodwastagereductionapp.customer.CustomerProfileActivity;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.PasswordHasher;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.manager.ManagerDashboardActivity;
import com.project.foodwastagereductionapp.manager.ManagerProfileActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/** Real UI + SQLite integration. Uses unique accounts and removes only those test rows. */
@RunWith(AndroidJUnit4.class)
public class AuthFlowTest {
    private static final String PASSWORD = "Coursework123";
    private String email;
    private String verificationTestCode;
    private Context context;
    private AppDatabaseHelper db;
    private ActivityScenario<SignUpActivity> scenario;

    @Before public void setUp() {
        verificationTestCode = InstrumentationRegistry.getArguments().getString("verificationTestCode");
        assumeTrue("Use a test email server and supply verificationTestCode for a configured build",
                !BuildConfig.EMAIL_VERIFICATION_URL.isEmpty() && verificationTestCode != null);
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = AppDatabaseHelper.getInstance(context);
        email = "sqlite-test-" + UUID.randomUUID() + "@example.com";
        new SessionManager(context).clear();
        scenario = ActivityScenario.launch(SignUpActivity.class);
    }
    @After public void cleanUp() {
        if (db == null) return;
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            for (Activity activity : ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED)) activity.finish();
        });
        if (scenario != null) scenario.close();
        new SessionManager(context).clear();
        db.getWritableDatabase().delete("users", "email = ?", new String[]{email});
    }

    @Test public void customerSignupLoginProfileLogoutAndRelogin() {
        signup(false);
        await(EmailVerificationActivity.class, R.id.btnVerificationContinue);
        onView(withId(R.id.txtVerificationMessage)).check(matches(withSubstring(email)));
        User pending = db.authenticate(email, PASSWORD);
        assertNotNull(pending);
        assertFalse(pending.emailVerified);
        verifyEmail(pending.id);
        await(LoginActivity.class, R.id.btnLogin);
        onView(withId(R.id.editEmail)).check(matches(withText(email)));
        onView(withId(R.id.editPassword)).perform(replaceText("wrong"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        await(LoginActivity.class, R.id.btnLogin);
        onView(withId(R.id.editPassword)).check(matches(hasErrorText(context.getString(R.string.msgInvalidCredentials))));
        login();
        await(CustomerHomeActivity.class, R.id.bottomNav);
        onView(withId(R.id.nav_profile)).perform(click());
        await(CustomerProfileActivity.class, R.id.layoutLogout);
        onView(withId(R.id.txtCustomerName)).check(matches(withText("Daeun Test")));
        onView(withId(R.id.txtEmail)).check(matches(withText(email)));
        onView(withId(R.id.txtAddress)).check(matches(withSubstring("Vancouver")));
        onView(withId(R.id.layoutLogout)).perform(click());
        await(LoginActivity.class, R.id.btnLogin);
        assertEquals(-1, new SessionManager(context).getUserId());
        login();
        await(CustomerHomeActivity.class, R.id.bottomNav);
        assertEquals(pending.id, new SessionManager(context).getUserId());
    }

    @Test public void managerSignupRestaurantSaveEditLogoutAndRelogin() {
        signup(true);
        await(EmailVerificationActivity.class, R.id.btnVerificationContinue);
        verifyEmail(db.authenticate(email, PASSWORD).id);
        await(RestaurantInformationActivity.class, R.id.btnGoToDashboard);
        onView(withId(R.id.editRestaurantCity)).check(matches(withText("Vancouver")));
        onView(withId(R.id.editRestaurantProvince)).check(matches(withText("BC")));
        onView(withId(R.id.editRestaurantPostalCode)).check(matches(withText("V6B 1A1")));
        fill(R.id.editRestaurantName, "SQLite Kitchen");
        fill(R.id.editRestaurantCity, "Burnaby");
        fill(R.id.editRestaurantPhone, "6045551234");
        fill(R.id.editCuisineType, "Korean");
        fill(R.id.editRestaurantDescription, "Food for local neighbours");
        onView(withId(R.id.btnGoToDashboard)).perform(scrollTo(), click());
        await(ManagerDashboardActivity.class, R.id.bottomNav);
        awaitRestaurantName("SQLite Kitchen");
        User manager = db.authenticate(email, PASSWORD);
        assertEquals("Burnaby", db.getRestaurantForManager(manager.id).city);
        long restaurantId = db.getRestaurantForManager(manager.id).id;
        onView(withId(R.id.nav_profile)).perform(click());
        await(ManagerProfileActivity.class, R.id.layoutEditProfile);
        awaitRestaurantName("SQLite Kitchen");
        onView(withId(R.id.txtHeaderTitle)).check(matches(withText(R.string.txtProfile)));
        onView(withId(R.id.nav_profile)).check(matches(isSelected()));
        captureManagerProfileIfRequested();
        onView(withId(R.id.layoutEditProfile)).perform(click());
        await(RestaurantInformationActivity.class, R.id.btnGoToDashboard);
        onView(withId(R.id.editRestaurantName)).check(matches(withText("SQLite Kitchen")));
        fill(R.id.editRestaurantName, "Renamed Kitchen");
        onView(withId(R.id.btnGoToDashboard)).perform(scrollTo(), click());
        await(ManagerProfileActivity.class, R.id.layoutEditProfile);
        awaitRestaurantName("Renamed Kitchen");
        assertEquals(restaurantId, db.getRestaurantForManager(manager.id).id);
        onView(withId(R.id.btnBack)).perform(click());
        await(ManagerDashboardActivity.class, R.id.bottomNav);
        onView(withId(R.id.nav_profile)).perform(click());
        await(ManagerProfileActivity.class, R.id.layoutEditProfile);
        onView(withId(R.id.nav_home)).perform(click());
        await(ManagerDashboardActivity.class, R.id.bottomNav);
        onView(withId(R.id.cardRestaurantProfile)).perform(scrollTo(), click());
        await(ManagerProfileActivity.class, R.id.layoutEditProfile);
        onView(withId(R.id.layoutLogout)).perform(click());
        await(LoginActivity.class, R.id.btnLogin);
        login();
        await(ManagerDashboardActivity.class, R.id.bottomNav);
        awaitRestaurantName("Renamed Kitchen");
    }

    @Test public void signingUpAgainResumesAnUnverifiedAccount() {
        long userId = db.createUser(email,
                PasswordHasher.hash(PASSWORD),
                User.CUSTOMER, "Daeun Test", "Vancouver", "BC", "V6B 1A1");
        enterSignupCredentials();
        await(EmailVerificationActivity.class, R.id.btnVerificationContinue);
        assertEquals(userId, db.authenticate(email, PASSWORD).id);
        onView(withId(R.id.btnVerificationContinue)).perform(scrollTo(), click());
        onView(withId(R.id.editVerificationCode)).check(matches(hasErrorText(
                context.getString(R.string.msgEnterEmailCode))));
        assertFalse(db.getUser(userId).emailVerified);
        assertEquals(-1, new SessionManager(context).getUserId());
        verifyEmail(userId);
        await(LoginActivity.class, R.id.btnLogin);
        assertTrue(db.getUser(userId).emailVerified);
    }

    private void verifyEmail(long userId) {
        String wrongCode = "000000".equals(verificationTestCode) ? "000001" : "000000";
        fill(R.id.editVerificationCode, wrongCode);
        onView(withId(R.id.btnVerificationContinue)).perform(scrollTo(), click());
        await(EmailVerificationActivity.class, R.id.btnVerificationContinue);
        onView(withId(R.id.editVerificationCode)).check(matches(hasErrorText(
                context.getString(R.string.msgIncorrectEmailCode))));
        assertFalse(db.getUser(userId).emailVerified);
        fill(R.id.editVerificationCode, verificationTestCode);
        onView(withId(R.id.btnVerificationContinue)).perform(scrollTo(), click());
    }

    private void captureManagerProfileIfRequested() {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("captureManagerProfile"))) return;
        Bitmap screenshot = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(screenshot);
        try (FileOutputStream output = new FileOutputStream(new File(context.getFilesDir(), "manager-profile-test.png"))) {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output));
        } catch (IOException error) {
            throw new AssertionError("Unable to save profile test screenshot", error);
        } finally {
            screenshot.recycle();
        }
    }

    private void enterSignupCredentials() {
        await(SignUpActivity.class, R.id.btnContinue);
        onView(withId(R.id.editEmail)).perform(replaceText(email));
        onView(withId(R.id.editPassword)).perform(replaceText(PASSWORD));
        onView(withId(R.id.editConfirmPassword)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.btnContinue)).perform(click());
    }

    private void signup(boolean manager) {
        enterSignupCredentials();
        Activity role = await(ChooseRoleActivity.class, R.id.btnContinue);
        if (manager) {
            onView(withId(R.id.cardManager)).perform(click());
            InstrumentationRegistry.getInstrumentation().runOnMainSync(role::recreate);
            awaitRecreatedRole(role);
        }
        onView(withId(R.id.btnContinue)).perform(click());
        await(AccountInformationActivity.class, R.id.btnCreateAccount);
        fill(R.id.editFullName, "Daeun Test");
        fill(R.id.editCity, "Vancouver");
        fill(R.id.editProvince, "BC");
        fill(R.id.editPostalCode, "V6B 1A1");
        onView(withId(R.id.btnCreateAccount)).perform(scrollTo(), click());
    }
    private void login() {
        onView(withId(R.id.editEmail)).perform(replaceText(email));
        onView(withId(R.id.editPassword)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
    }
    private void fill(int id, String value) {
        onView(withId(id)).perform(scrollTo(), replaceText(value), closeSoftKeyboard());
    }
    private Activity await(Class<?> screen, int readyViewId) {
        long deadline = SystemClock.uptimeMillis() + 15000;
        AtomicReference<Activity> found = new AtomicReference<>();
        while (SystemClock.uptimeMillis() < deadline) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                for (Activity activity : ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED)) {
                    View view = activity.findViewById(readyViewId);
                    if (screen.isInstance(activity) && view != null && view.isEnabled()) found.set(activity);
                }
            });
            if (found.get() != null) { InstrumentationRegistry.getInstrumentation().waitForIdleSync(); return found.get(); }
            SystemClock.sleep(50);
        }
        throw new AssertionError("Timed out waiting for " + screen.getSimpleName());
    }
    private void awaitRecreatedRole(Activity old) {
        long deadline = SystemClock.uptimeMillis() + 15000;
        while (SystemClock.uptimeMillis() < deadline) {
            Activity current = await(ChooseRoleActivity.class, R.id.btnContinue);
            if (current != old) return;
            SystemClock.sleep(50);
        }
        throw new AssertionError("Role screen was not recreated");
    }
    private void awaitRestaurantName(String name) {
        long deadline = SystemClock.uptimeMillis() + 15000;
        while (SystemClock.uptimeMillis() < deadline) {
            try { onView(withId(R.id.txtRestaurantName)).check(matches(withText(name))); return; }
            catch (AssertionError error) { SystemClock.sleep(50); }
        }
        onView(withId(R.id.txtRestaurantName)).check(matches(withText(name)));
    }
}
