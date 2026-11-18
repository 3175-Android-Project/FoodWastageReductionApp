package com.project.foodwastagereductionapp.auth;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import com.project.foodwastagereductionapp.BuildConfig;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.customer.*;
import com.project.foodwastagereductionapp.data.*;
import com.project.foodwastagereductionapp.manager.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

@RunWith(AndroidJUnit4.class)
public class AccountFeaturesUiTest {
    private Context context; private AppDatabaseHelper db; private SessionManager session;
    private long customer, manager; private String email;
    @Before public void setup() {
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        assumeTrue("Use the separate account-test APK",context.getPackageName().endsWith(".accounttest"));
        db=AppDatabaseHelper.getInstance(context);session=new SessionManager(context);session.clear();
        String tag=UUID.randomUUID().toString();email="account-"+tag+"@example.com";
        String hash=PasswordHasher.hash("Original123");
        customer=db.createUser(email,hash,User.CUSTOMER,"Customer A","Vancouver","BC","V6B 1A1","405 Main Street","");
        manager=db.createUser("manager-"+tag+"@example.com",hash,User.MANAGER,"Manager A","Vancouver","BC","V6B 1A1","100 Manager St","");
        db.confirmEmailLocally(customer);db.confirmEmailLocally(manager);
        db.saveRestaurant(new Restaurant(0,manager,"Account Kitchen","Vancouver","BC","V6B1A1","6045550100","Korean","",""));
    }
    @After public void cleanup() {
        if(session==null)return;
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)) a.finish();
        });session.clear();
    }
    private Activity await(Class<?> type,int id) {
        long end=SystemClock.uptimeMillis()+15000;AtomicReference<Activity> found=new AtomicReference<>();
        while(SystemClock.uptimeMillis()<end){
            InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
                for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)){
                    View v=a.findViewById(id);if(type.isInstance(a)&&v!=null&&v.isEnabled()) found.set(a);
                }
            });if(found.get()!=null){InstrumentationRegistry.getInstrumentation().waitForIdleSync();return found.get();}SystemClock.sleep(50);
        }throw new AssertionError("Timeout "+type.getSimpleName());
    }
    private void visible(int id){try{onView(withId(id)).perform(scrollTo());}catch(androidx.test.espresso.PerformException ignored){}}
    private void fill(int id,String text){visible(id);onView(withId(id)).perform(replaceText(text),closeSoftKeyboard());}
    private void tap(int id){visible(id);onView(withId(id)).perform(click());}
    private void capture(String name) {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("captureAccountScreens"))) return;
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(250);
        File directory=new File(context.getFilesDir(),"account-test-screens");directory.mkdirs();
        Bitmap image=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        try(OutputStream output=new FileOutputStream(new File(directory,name))) { image.compress(Bitmap.CompressFormat.PNG,100,output); }
        catch(IOException error){throw new AssertionError(error);}finally{image.recycle();}
    }
    private void awaitProfileValue(long id,String name){long end=SystemClock.uptimeMillis()+10000;while(SystemClock.uptimeMillis()<end){if(name.equals(db.getUser(id).fullName))return;SystemClock.sleep(50);}fail("Profile not saved");}

    @Test public void editProfileDetailsSurviveRotationAndReopenWithoutPhotoControls() {
        session.start(customer,false);
        ActivityScenario.launch(CustomerProfileActivity.class);
        await(CustomerProfileActivity.class,R.id.layoutChangePassword);
        tap(R.id.layoutEditProfile);
        await(EditProfileActivity.class,R.id.btnSaveChanges);
        onView(withId(R.id.btnChoosePhoto)).check(doesNotExist());
        onView(withId(R.id.btnRemovePhoto)).check(doesNotExist());
        fill(R.id.inputFullName,"Customer Edited");fill(R.id.etAddress,"123 New Street");
        fill(R.id.etPhone,"6045550123");
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))
                if(a instanceof EditProfileActivity)a.recreate();
        });
        await(EditProfileActivity.class,R.id.btnSaveChanges);
        onView(withId(R.id.etAddress)).check(matches(withText("123 New Street")));
        onView(withId(R.id.etPhone)).check(matches(withText("6045550123")));
        capture("account-edit-profile.png");
        tap(R.id.btnSaveChanges);awaitProfileValue(customer,"Customer Edited");
        await(CustomerProfileActivity.class,R.id.layoutChangePassword);
        User saved=db.getUser(customer);
        assertEquals("123 New Street",saved.address);assertEquals("6045550123",saved.phone);
        onView(withId(R.id.txtCustomerName)).check(matches(withText("Customer Edited")));
        onView(withId(R.id.txtAddress)).check(matches(withSubstring("123 New Street")));
        tap(R.id.layoutEditProfile);await(EditProfileActivity.class,R.id.btnSaveChanges);
        onView(withId(R.id.inputFullName)).check(matches(withText("Customer Edited")));
        onView(withId(R.id.etAddress)).check(matches(withText("123 New Street")));
        onView(withId(R.id.etPhone)).check(matches(withText("6045550123")));
        onView(withId(R.id.btnChoosePhoto)).check(doesNotExist());
        assertEquals("Manager A",db.getUser(manager).fullName);
    }
    @Test public void changePasswordRejectsWrongCurrentAndMismatchThenSignsOut() {
        session.start(customer,false);
        ActivityScenario<CustomerProfileActivity> scenario=ActivityScenario.launch(CustomerProfileActivity.class);
        await(CustomerProfileActivity.class,R.id.layoutChangePassword);
        scenario.onActivity(activity->{
            android.view.ViewGroup card=(android.view.ViewGroup)activity.findViewById(R.id.layoutEditProfile).getParent();
            int edit=card.indexOfChild(activity.findViewById(R.id.layoutEditProfile));
            int change=card.indexOfChild(activity.findViewById(R.id.layoutChangePassword));
            int logout=card.indexOfChild(activity.findViewById(R.id.layoutLogout));
            assertTrue(edit<change && change<logout);
        });
        capture("customer-profile.png");
        tap(R.id.layoutChangePassword);await(ChangePasswordActivity.class,R.id.btnChangePassword);
        capture("change-password.png");
        fill(R.id.editCurrentPassword,"wrong");fill(R.id.editNewPassword,"Changed123");fill(R.id.editConfirmNewPassword,"Changed123");tap(R.id.btnChangePassword);await(ChangePasswordActivity.class,R.id.btnChangePassword);
        onView(withId(R.id.editCurrentPassword)).check(matches(hasErrorText(context.getString(R.string.msgIncorrectPassword))));
        assertNotNull(db.authenticate(email,"Original123"));
        fill(R.id.editCurrentPassword,"Original123");fill(R.id.editConfirmNewPassword,"different");tap(R.id.btnChangePassword);
        onView(withId(R.id.editConfirmNewPassword)).check(matches(hasErrorText(context.getString(R.string.msgPasswordsDoNotMatch))));
        fill(R.id.editConfirmNewPassword,"Changed123");tap(R.id.btnChangePassword);await(LoginActivity.class,R.id.btnLogin);
        assertEquals(-1,session.getUserId());assertNull(db.authenticate(email,"Original123"));
        fill(R.id.editEmail,email);fill(R.id.editPassword,"Changed123");tap(R.id.btnLogin);await(CustomerHomeActivity.class,R.id.bottomNav);
        assertEquals(customer,session.getUserId());
        tap(R.id.nav_profile);await(CustomerProfileActivity.class,R.id.layoutChangePassword);
        tap(R.id.layoutLogout);tap(R.id.dialogPositive);await(LoginActivity.class,R.id.btnLogin);
        assertEquals(-1,session.getUserId());
    }
    @Test public void managerProfileChangesPasswordWithoutChangingRestaurant() {
        session.start(manager,false);
        ActivityScenario<ManagerProfileActivity> scenario=ActivityScenario.launch(ManagerProfileActivity.class);
        await(ManagerProfileActivity.class,R.id.layoutChangePassword);
        onView(withText("Account settings")).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist());
        scenario.onActivity(activity->{
            android.view.ViewGroup card=(android.view.ViewGroup)activity.findViewById(R.id.layoutEditProfile).getParent();
            int edit=card.indexOfChild(activity.findViewById(R.id.layoutEditProfile));
            int change=card.indexOfChild(activity.findViewById(R.id.layoutChangePassword));
            int logout=card.indexOfChild(activity.findViewById(R.id.layoutLogout));
            assertTrue(edit<change && change<logout);
        });
        capture("manager-profile.png");
        tap(R.id.layoutChangePassword);
        await(ChangePasswordActivity.class,R.id.btnChangePassword);
        fill(R.id.editCurrentPassword,"Original123");
        fill(R.id.editNewPassword,"ManagerChanged123");
        fill(R.id.editConfirmNewPassword,"ManagerChanged123");
        tap(R.id.btnChangePassword);
        await(LoginActivity.class,R.id.btnLogin);
        assertEquals(-1,session.getUserId());
        assertNull(db.authenticate(db.getUser(manager).email,"Original123"));
        assertNotNull(db.authenticate(db.getUser(manager).email,"ManagerChanged123"));
        assertEquals("Account Kitchen",db.getRestaurantForManager(manager).name);
    }
    @Test public void missingEmailDoesNotAdvanceToCodeScreen() {
        ActivityScenario.launch(ForgotPasswordActivity.class);fill(R.id.editEmail,"unknown-"+UUID.randomUUID()+"@example.com");tap(R.id.btnSendResetLink);SystemClock.sleep(500);
        onView(withId(R.id.editEmail)).check(matches(hasErrorText(context.getString(R.string.msgAccountNotFound))));
    }
    @Test public void emailOnlyResetScreenCannotChangePassword() {
        ActivityScenario.launch(new Intent(context,ResetPasswordActivity.class).putExtra("email",email));SystemClock.sleep(500);
        assertNotNull(db.authenticate(email,"Original123"));
        AtomicBoolean present=new AtomicBoolean();InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)) if(a instanceof ResetPasswordActivity)present.set(true);
        });assertFalse(present.get());
    }
    @Test public void uppercasePasswordResetValidatesMismatchAndLogsInWithNewPassword() {
        assumeTrue(BuildConfig.EMAIL_VERIFICATION_URL.equals("http://10.0.2.2:8766"));
        ActivityScenario.launch(ForgotPasswordActivity.class);fill(R.id.editEmail,"  "+email.toUpperCase(Locale.ROOT)+"  ");tap(R.id.btnSendResetLink);
        await(ResetLinkSentActivity.class,R.id.btnVerifyResetCode);fill(R.id.editResetCode,"000000");tap(R.id.btnVerifyResetCode);SystemClock.sleep(500);
        onView(withId(R.id.editResetCode)).check(matches(hasErrorText(context.getString(R.string.msgIncorrectEmailCode))));
        fill(R.id.editResetCode,"123456");tap(R.id.btnVerifyResetCode);await(ResetPasswordActivity.class,R.id.btnResetPassword);
        fill(R.id.editNewPassword,"short");fill(R.id.editConfirmNewPassword,"short");tap(R.id.btnResetPassword);
        onView(withId(R.id.editNewPassword)).check(matches(hasErrorText(context.getString(R.string.msgPasswordTooShort))));
        fill(R.id.editNewPassword,"Reset123");fill(R.id.editConfirmNewPassword,"Mismatch123");tap(R.id.btnResetPassword);
        onView(withId(R.id.editConfirmNewPassword)).check(matches(hasErrorText(context.getString(R.string.msgPasswordsDoNotMatch))));
        fill(R.id.editConfirmNewPassword,"Reset123");tap(R.id.btnResetPassword);await(PasswordResetCompleteActivity.class,R.id.btnGoToLogin);
        tap(R.id.btnGoToLogin);await(LoginActivity.class,R.id.btnLogin);fill(R.id.editEmail,email);fill(R.id.editPassword,"Reset123");tap(R.id.btnLogin);await(CustomerHomeActivity.class,R.id.bottomNav);
        assertNull(db.authenticate(email,"Original123"));assertEquals(customer,session.getUserId());
    }
}
