package com.project.foodwastagereductionapp.auth;

import android.content.Context;
import android.content.SharedPreferences;

/** Keeps only the current user ID; roles and profile details are always read from Room. */
public final class SessionManager {
    private final SharedPreferences preferences;
    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences("grabbit_session", Context.MODE_PRIVATE);
    }
    public long getUserId() { return preferences.getLong("userId", -1); }
    public boolean shouldAutoLogin() {
        return preferences.getBoolean("keepSignedIn", false);
    }
    public void start(long userId, boolean keepSignedIn) {
        preferences.edit()
                .putLong("userId", userId)
                .putBoolean("keepSignedIn", keepSignedIn)
                .apply();
    }
    public void clear() { preferences.edit().clear().apply(); }
}
