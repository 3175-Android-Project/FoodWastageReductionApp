package com.project.foodwastagereductionapp.auth;

import android.content.Context;
import android.content.SharedPreferences;
import com.project.foodwastagereductionapp.customer.CartManager;
import com.project.foodwastagereductionapp.customer.CustomerBrowseActivity;
import com.project.foodwastagereductionapp.customer.CustomerHomeActivity;
import com.project.foodwastagereductionapp.customer.CustomerOrderActivity;
import com.project.foodwastagereductionapp.customer.CustomerProfileActivity;
import com.project.foodwastagereductionapp.manager.ManagerDashboardActivity;

/** Identity stays in preferences; private cached data belongs to one session generation. */
public final class SessionManager {
    private static final Object LOCK = new Object();
    private static long generation;
    private final Context context;
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        preferences = this.context
                .getSharedPreferences("grabbit_session", Context.MODE_PRIVATE);
        long userId = getUserId();
        if (userId != -1) {
            CartManager.init(this.context, userId);
        }
    }

    public long getUserId() { return preferences.getLong("userId", -1); }
    public boolean shouldAutoLogin() { return preferences.getBoolean("keepSignedIn", false); }

    public void start(long userId, boolean keepSignedIn) {
        synchronized (LOCK) {
            generation++;
            preferences.edit().putLong("userId", userId).putBoolean("keepSignedIn", keepSignedIn).apply();
            invalidateCaches();
            CartManager.saveAndLoadUser(context, userId);
        }
    }

    public void clear() {
        synchronized (LOCK) {
            generation++;
            preferences.edit().clear().apply();
            invalidateCaches();
            CartManager.saveAndClear(context);
        }
    }
    public Snapshot snapshot() {
        synchronized (LOCK) { return new Snapshot(getUserId(), generation); }
    }
    public boolean isCurrent(Snapshot snapshot) {
        synchronized (LOCK) { return snapshot != null && snapshot.userId == getUserId()
                && snapshot.generation == generation; }
    }
    public static void invalidateCaches() {
        CustomerHomeActivity.invalidateCache();
        CustomerOrderActivity.invalidateCache();
        CustomerBrowseActivity.invalidateCache();
        CustomerProfileActivity.invalidateCache();
        ManagerDashboardActivity.invalidateCache();
    }
    public static final class Snapshot {
        public final long userId;
        private final long generation;
        private Snapshot(long userId, long generation) { this.userId = userId; this.generation = generation; }
    }
}
