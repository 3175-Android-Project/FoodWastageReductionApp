package com.project.foodwastagereductionapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.project.foodwastagereductionapp.auth.AuthNavigator;
import com.project.foodwastagereductionapp.auth.EmailVerificationActivity;
import com.project.foodwastagereductionapp.auth.RestaurantInformationActivity;
import com.project.foodwastagereductionapp.customer.CustomerHomeActivity;
import com.project.foodwastagereductionapp.manager.ManagerDashboardActivity;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.PasswordHasher;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseHelperTest {
    private static final String TEST_DB = "grabbit-instrumentation-test.db";
    private Context context;
    private AppDatabaseHelper db;
    private String hash;
    @Before public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DB);
        db = new AppDatabaseHelper(context, TEST_DB);
        hash = PasswordHasher.hash("Coursework123");
    }
    @After public void tearDown() {
        db.close();
        context.deleteDatabase(TEST_DB);
    }
    private long user(String email, String role) {
        return db.createUser(email, hash, role, "Daeun", "Vancouver", "BC", "V6B 1A1");
    }
    private Restaurant restaurant(long manager, String name) {
        return new Restaurant(0, manager, name, "Burnaby", "BC", "V5A 1S6",
                "6045551234", "Korean", "Fresh surplus food");
    }
    @Test public void customerPersistsAfterDatabaseReopenAndAuthenticates() {
        long id = user("  Customer@Example.com  ", User.CUSTOMER);
        db.close();
        db = new AppDatabaseHelper(context, TEST_DB);
        User saved = db.authenticate("CUSTOMER@example.com", "Coursework123");
        assertNotNull(saved);
        assertEquals(id, saved.id);
        assertEquals("customer@example.com", saved.email);
        assertEquals("Daeun", saved.fullName);
        assertEquals("Vancouver", saved.city);
        assertEquals("BC", saved.province);
        assertEquals("V6B 1A1", saved.postalCode);
        assertEquals(User.CUSTOMER, saved.role);
        assertFalse(saved.emailVerified);
    }
    @Test public void emailUniquenessIsCaseInsensitiveWithoutReplacingOriginalAccount() {
        long id = user("same@example.com", User.CUSTOMER);
        try {
            user(" SAME@EXAMPLE.COM ", User.MANAGER);
            fail("Duplicate email must be rejected");
        } catch (AppDatabaseHelper.DuplicateEmailException expected) { }
        assertEquals(id, db.authenticate("same@example.com", "Coursework123").id);
        assertEquals(User.CUSTOMER, db.getUser(id).role);
    }
    @Test public void invalidCredentialsAndSqlInputDoNotAuthenticate() {
        user("user@example.com", User.CUSTOMER);
        assertNull(db.authenticate("user@example.com", "wrong"));
        assertNull(db.authenticate("user@example.com", ""));
        assertNull(db.authenticate("missing@example.com", "Coursework123"));
        assertNull(db.authenticate("' OR 1=1 --", "Coursework123"));
    }
    @Test public void pendingCustomerResumesVerificationThenRoutesHome() {
        long id = user("customer@example.com", User.CUSTOMER);
        assertEquals(EmailVerificationActivity.class, AuthNavigator.destination(db, db.getUser(id)));
        db.confirmEmailLocally(id);
        db.close();
        db = new AppDatabaseHelper(context, TEST_DB);
        assertTrue(db.getUser(id).emailVerified);
        assertEquals(CustomerHomeActivity.class, AuthNavigator.destination(db, db.getUser(id)));
    }
    @Test public void managerResumesEachOnboardingStageAndKeepsRestaurant() {
        long id = user("manager@example.com", User.MANAGER);
        assertEquals(EmailVerificationActivity.class, AuthNavigator.destination(db, db.getUser(id)));
        db.confirmEmailLocally(id);
        assertEquals(RestaurantInformationActivity.class, AuthNavigator.destination(db, db.getUser(id)));
        db.saveRestaurant(restaurant(id, "Grabbit Kitchen"));
        db.close();
        db = new AppDatabaseHelper(context, TEST_DB);
        assertEquals(ManagerDashboardActivity.class, AuthNavigator.destination(db, db.getUser(id)));
        Restaurant saved = db.getRestaurantForManager(id);
        assertEquals(id, saved.managerId);
        assertEquals("Grabbit Kitchen", saved.name);
        assertEquals("Burnaby", saved.city);
        assertEquals("BC", saved.province);
        assertEquals("V5A 1S6", saved.postalCode);
        assertEquals("6045551234", saved.phone);
        assertEquals("Korean", saved.cuisineType);
        assertEquals("Fresh surplus food", saved.description);
    }
    @Test public void repeatRestaurantSaveUpdatesOneRowWithoutChangingOwner() {
        long id = user("manager@example.com", User.MANAGER);
        db.confirmEmailLocally(id);
        db.saveRestaurant(restaurant(id, "First"));
        long restaurantId = db.getRestaurantForManager(id).id;
        db.saveRestaurant(restaurant(id, "Renamed"));
        assertEquals(restaurantId, db.getRestaurantForManager(id).id);
        assertEquals("Renamed", db.getRestaurantForManager(id).name);
        try (Cursor c = db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM restaurants", null)) {
            assertTrue(c.moveToFirst());
            assertEquals(1, c.getInt(0));
        }
    }
    @Test public void rejectsCustomerUnconfirmedManagerAndMissingOwner() {
        long customer = user("customer@example.com", User.CUSTOMER);
        db.confirmEmailLocally(customer);
        long pending = user("pending@example.com", User.MANAGER);
        for (long id : new long[]{customer, pending, 999999}) {
            try { db.saveRestaurant(restaurant(id, "Invalid")); fail("Must reject owner"); }
            catch (IllegalArgumentException expected) { }
            assertNull(db.getRestaurantForManager(id));
        }
    }
    @Test public void invalidRestaurantDoesNotOverwriteExistingData() {
        long id = user("manager@example.com", User.MANAGER);
        db.confirmEmailLocally(id);
        db.saveRestaurant(restaurant(id, "Original"));
        try { db.saveRestaurant(restaurant(id, "   ")); fail("Required name"); }
        catch (IllegalArgumentException expected) { }
        assertEquals("Original", db.getRestaurantForManager(id).name);
    }
    @Test public void foreignKeyIsEnforcedAndCascadeDeletesRestaurant() {
        ContentValues invalid = new ContentValues();
        invalid.put("manager_id", 999999);
        invalid.put("name", "Invalid");
        invalid.put("city", "City"); invalid.put("province", "BC");
        invalid.put("postal_code", "V6B 1A1"); invalid.put("phone", "123");
        invalid.put("cuisine_type", "Test");
        try { db.getWritableDatabase().insertOrThrow("restaurants", null, invalid); fail("Foreign key"); }
        catch (SQLiteConstraintException expected) { }
        long id = user("manager@example.com", User.MANAGER);
        db.confirmEmailLocally(id);
        db.saveRestaurant(restaurant(id, "Kitchen"));
        db.getWritableDatabase().delete("users", "id = ?", new String[]{Long.toString(id)});
        assertNull(db.getRestaurantForManager(id));
    }
    @Test public void managerAccountsKeepSeparateRestaurants() {
        long first = user("first@example.com", User.MANAGER);
        long second = user("second@example.com", User.MANAGER);
        db.confirmEmailLocally(first); db.confirmEmailLocally(second);
        db.saveRestaurant(restaurant(first, "First"));
        db.saveRestaurant(restaurant(second, "Second"));
        assertEquals("First", db.getRestaurantForManager(first).name);
        assertEquals("Second", db.getRestaurantForManager(second).name);
    }
}
