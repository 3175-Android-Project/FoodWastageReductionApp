package com.project.foodwastagereductionapp.auth;

import android.app.Activity;
import android.content.Intent;
import com.project.foodwastagereductionapp.customer.CustomerHomeActivity;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.manager.ManagerDashboardActivity;

public final class AuthNavigator {
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_PASSWORD_HASH = "passwordHash";
    private AuthNavigator() { }

    /** Call on the DB worker. Role and onboarding state come from saved data. */
    public static Class<?> destination(AppDatabaseHelper db, User user) {
        if (!user.emailVerified) return EmailVerificationActivity.class;
        if (User.MANAGER.equals(user.role)) {
            return db.getRestaurantForManager(user.id) == null
                    ? RestaurantInformationActivity.class : ManagerDashboardActivity.class;
        }
        return CustomerHomeActivity.class;
    }

    public static void open(Activity activity, Class<?> screen, long userId) {
        Intent intent = new Intent(activity, screen);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void logout(Activity activity) {
        new SessionManager(activity).clear();
        open(activity, LoginActivity.class, -1);
    }
}
