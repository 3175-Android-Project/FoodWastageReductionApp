package com.project.foodwastagereductionapp;

import android.content.Context;
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
import com.project.foodwastagereductionapp.data.FoodItemEntity;
import com.project.foodwastagereductionapp.data.OrderLine;
import com.project.foodwastagereductionapp.data.OrderStatus;
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
                "6045551234", "Korean", "Fresh surplus food", "");
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
    @Test public void invalidCredentialsAndSpecialInputDoNotAuthenticate() {
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
        assertEquals(1, db.countRestaurants());
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
        assertNull(db.getRestaurantForManager(999999));
        long id = user("manager@example.com", User.MANAGER);
        db.confirmEmailLocally(id);
        db.saveRestaurant(restaurant(id, "Kitchen"));
        assertEquals(1, db.countRestaurants());
        db.deleteUser(id);
        assertNull(db.getRestaurantForManager(id));
        assertEquals(0, db.countRestaurants());
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
    @Test public void cartItemsPersistInRoomDatabaseAndCascadeOnUserDelete() {
        long userId = user("cartcustomer@example.com", User.CUSTOMER);
        com.project.foodwastagereductionapp.data.FoodItem food = new com.project.foodwastagereductionapp.data.FoodItem(1, "Sushi", 8.0, 7.5, 5, "5:00 PM", 0, "Description");
        db.saveCartItem(userId, 101L, food, 2);
        assertEquals(1, db.getCartItemsForUser(userId).size());
        assertEquals(2, db.getCartItemsForUser(userId).get(0).quantity);
        db.deleteUser(userId);
        assertEquals(0, db.getCartItemsForUser(userId).size());
    }

    @Test public void managerFoodLifecycleAndStockAvailability() {
        long manager = user("manager@example.com", User.MANAGER);
        db.confirmEmailLocally(manager);
        db.saveRestaurant(restaurant(manager, "Test Kitchen"));
        long restaurantId = db.getRestaurantForManager(manager).id;

        long foodId = db.createFoodItem(
                restaurantId, "Soup", "Fresh soup", "Soup",
                10.0, 6.0, 3, "8:00 PM", true, "");

        FoodItemEntity food = db.getFoodItem(foodId);
        assertEquals(3, food.quantityLeft);
        assertTrue(food.available);

        food.quantityLeft = 5;
        food.name = "Lunch Soup";
        db.updateFoodItem(food);
        assertEquals("Lunch Soup", db.getFoodItem(foodId).name);
        assertEquals(5, db.getFoodItem(foodId).quantityLeft);

        db.deleteFoodItem(foodId);
        assertNull(db.getFoodItem(foodId));
    }

    @Test public void checkoutReservesStockAndCancellationRestoresIt() {
        long customer = user("customer@example.com", User.CUSTOMER);
        long manager = user("manager@example.com", User.MANAGER);
        db.confirmEmailLocally(customer);
        db.confirmEmailLocally(manager);
        db.saveRestaurant(restaurant(manager, "Test Kitchen"));
        long restaurantId = db.getRestaurantForManager(manager).id;
        long foodId = db.createFoodItem(
                restaurantId, "Sandwich", "Surplus sandwich", "Lunch",
                8.0, 5.0, 2, "7:00 PM", true, "");

        long orderId = db.placeOrder(
                customer, restaurantId,
                java.util.Collections.singletonList(new OrderLine((int) foodId, "Sandwich", 5.0, 1)),
                "7:00 PM", false, null);

        assertEquals(1, db.getFoodItem(foodId).quantityLeft);
        assertTrue(db.cancelOrder(customer, orderId));
        assertEquals(2, db.getFoodItem(foodId).quantityLeft);
        assertFalse(db.cancelOrder(customer, orderId));
    }

    @Test public void rejectedOrderRestoresStockAndNotifiesCustomer() {
        long customer = user("customer@example.com", User.CUSTOMER);
        long manager = user("manager@example.com", User.MANAGER);
        db.confirmEmailLocally(customer);
        db.confirmEmailLocally(manager);
        db.saveRestaurant(restaurant(manager, "Test Kitchen"));
        long restaurantId = db.getRestaurantForManager(manager).id;
        long foodId = db.createFoodItem(
                restaurantId, "Pasta", "Surplus pasta", "Dinner",
                12.0, 7.0, 2, "8:00 PM", true, "");
        long orderId = db.placeOrder(
                customer, restaurantId,
                java.util.Collections.singletonList(new OrderLine((int) foodId, "Pasta", 7.0, 1)),
                "8:00 PM", false, null);

        assertTrue(db.updateOrderStatusForRestaurant(
                manager, orderId, OrderStatus.REJECTED));
        assertEquals(2, db.getFoodItem(foodId).quantityLeft);
        assertEquals(OrderStatus.REJECTED, db.getOrders(customer).get(0).status);
        assertEquals(1, db.getNotifications(customer).size());
    }

    @Test public void managerCannotChangeAnotherRestaurantsOrder() {
        long customer = user("customer@example.com", User.CUSTOMER);
        long firstManager = user("first@example.com", User.MANAGER);
        long secondManager = user("second@example.com", User.MANAGER);
        db.confirmEmailLocally(customer);
        db.confirmEmailLocally(firstManager);
        db.confirmEmailLocally(secondManager);
        db.saveRestaurant(restaurant(firstManager, "First Kitchen"));
        db.saveRestaurant(restaurant(secondManager, "Second Kitchen"));
        long restaurantId = db.getRestaurantForManager(firstManager).id;
        long foodId = db.createFoodItem(
                restaurantId, "Rice", "Surplus rice", "Dinner",
                10.0, 4.0, 2, "8:00 PM", true, "");
        long orderId = db.placeOrder(
                customer, restaurantId,
                java.util.Collections.singletonList(new OrderLine((int) foodId, "Rice", 4.0, 1)),
                "8:00 PM", false, null);

        assertFalse(db.updateOrderStatusForRestaurant(
                secondManager, orderId, OrderStatus.ACCEPTED));
        assertEquals(OrderStatus.PENDING, db.getOrders(customer).get(0).status);
    }
}
