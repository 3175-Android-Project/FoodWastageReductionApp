package com.project.foodwastagereductionapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Patterns;
import java.util.Locale;

/** Shared SQLite connection. Call from a worker thread, not the main UI thread. */
public final class AppDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "grabbit.db";
    private static final int DATABASE_VERSION = 1;
    private static AppDatabaseHelper instance;
    private static final String[] USER_COLUMNS = {"id", "email", "role", "full_name",
            "city", "province", "postal_code", "email_verified"};

    public static synchronized AppDatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new AppDatabaseHelper(context, DATABASE_NAME);
        return instance;
    }

    // A separate database name allows instrumentation tests without touching app accounts.
    public AppDatabaseHelper(Context context, String databaseName) {
        super(context.getApplicationContext(), databaseName, null, DATABASE_VERSION);
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "email TEXT NOT NULL COLLATE NOCASE UNIQUE,"
                + "password_hash TEXT NOT NULL,"
                + "role TEXT NOT NULL CHECK(role IN ('customer','manager')),"
                + "full_name TEXT NOT NULL, city TEXT NOT NULL,"
                + "province TEXT NOT NULL, postal_code TEXT NOT NULL,"
                + "email_verified INTEGER NOT NULL DEFAULT 0 CHECK(email_verified IN (0,1)))");
        db.execSQL("CREATE TABLE restaurants ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "manager_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,"
                + "name TEXT NOT NULL, city TEXT NOT NULL, province TEXT NOT NULL,"
                + "postal_code TEXT NOT NULL, phone TEXT NOT NULL, cuisine_type TEXT NOT NULL,"
                + "description TEXT NOT NULL DEFAULT '')");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first schema. Future versions must add explicit, non-destructive migrations.
        throw new IllegalStateException("Missing migration from " + oldVersion + " to " + newVersion);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean validEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(normalizeEmail(email)).matches();
    }

    public boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    /** Hash is generated before entering this method; plaintext passwords are never stored. */
    public long createUser(String email, String passwordHash, String role, String fullName,
                           String city, String province, String postalCode) {
        if (!validEmail(email) || passwordHash == null || !passwordHash.startsWith("pbkdf2_sha1$")
                || !(User.CUSTOMER.equals(role) || User.MANAGER.equals(role))) {
            throw new IllegalArgumentException("Invalid signup details");
        }
        ContentValues values = new ContentValues();
        values.put("email", normalizeEmail(email));
        values.put("password_hash", passwordHash);
        values.put("role", role);
        values.put("full_name", required(fullName));
        values.put("city", required(city));
        values.put("province", required(province));
        values.put("postal_code", required(postalCode));
        try {
            return getWritableDatabase().insertOrThrow("users", null, values);
        } catch (SQLiteConstraintException e) {
            if (emailExists(email)) throw new DuplicateEmailException();
            throw e;
        }
    }

    /** Also returns pending accounts so login can resume interrupted verification/onboarding. */
    public User authenticate(String email, String password) {
        try (Cursor cursor = getReadableDatabase().query("users", null, "email = ?",
                new String[]{normalizeEmail(email)}, null, null, null)) {
            if (!cursor.moveToFirst() || !PasswordHasher.verify(password,
                    cursor.getString(cursor.getColumnIndexOrThrow("password_hash")))) return null;
            return readUser(cursor);
        }
    }

    public User getUser(long id) {
        return queryUser("id = ?", new String[]{Long.toString(id)});
    }

    private User getUserByEmail(String email) {
        return queryUser("email = ?", new String[]{normalizeEmail(email)});
    }

    private User queryUser(String where, String[] args) {
        try (Cursor cursor = getReadableDatabase().query("users", USER_COLUMNS,
                where, args, null, null, null)) {
            return cursor.moveToFirst() ? readUser(cursor) : null;
        }
    }

    /** Store the confirmation after the server verifies the code. */
    public User confirmEmailLocally(long userId) {
        ContentValues values = new ContentValues();
        values.put("email_verified", 1);
        if (getWritableDatabase().update("users", values, "id = ?",
                new String[]{Long.toString(userId)}) != 1) {
            throw new IllegalArgumentException("Account no longer exists");
        }
        return getUser(userId);
    }

    /** Idempotent save: revisiting restaurant setup updates the same row. */
    public void saveRestaurant(Restaurant restaurant) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            User owner = getUser(restaurant.managerId);
            if (owner == null || !User.MANAGER.equals(owner.role) || !owner.emailVerified) {
                throw new IllegalArgumentException("A confirmed manager account is required");
            }
            ContentValues values = new ContentValues();
            values.put("manager_id", restaurant.managerId);
            values.put("name", required(restaurant.name));
            values.put("city", required(restaurant.city));
            values.put("province", required(restaurant.province));
            values.put("postal_code", required(restaurant.postalCode));
            values.put("phone", required(restaurant.phone));
            values.put("cuisine_type", required(restaurant.cuisineType));
            values.put("description", restaurant.description == null ? "" : restaurant.description.trim());
            int count = db.update("restaurants", values, "manager_id = ?",
                    new String[]{Long.toString(restaurant.managerId)});
            if (count == 0) db.insertOrThrow("restaurants", null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Restaurant getRestaurantForManager(long managerId) {
        try (Cursor c = getReadableDatabase().query("restaurants", null, "manager_id = ?",
                new String[]{Long.toString(managerId)}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            return new Restaurant(c.getLong(c.getColumnIndexOrThrow("id")), managerId,
                    string(c, "name"), string(c, "city"), string(c, "province"),
                    string(c, "postal_code"), string(c, "phone"), string(c, "cuisine_type"),
                    string(c, "description"));
        }
    }

    private static User readUser(Cursor c) {
        return new User(c.getLong(c.getColumnIndexOrThrow("id")), string(c, "email"),
                string(c, "role"), string(c, "full_name"), string(c, "city"),
                string(c, "province"), string(c, "postal_code"),
                c.getInt(c.getColumnIndexOrThrow("email_verified")) == 1);
    }

    private static String string(Cursor c, String column) {
        return c.getString(c.getColumnIndexOrThrow(column));
    }

    private static String required(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Required field");
        return value.trim();
    }

    public static final class DuplicateEmailException extends IllegalArgumentException {
        public DuplicateEmailException() { super("Email already registered"); }
    }

    public boolean updatePasswordByEmail(String email, String newPasswordHash) {

        if (!validEmail(email)
                || newPasswordHash == null
                || !newPasswordHash.startsWith("pbkdf2_sha1$")) {
            throw new IllegalArgumentException("Invalid password reset details");
        }

        ContentValues values = new ContentValues();
        values.put("password_hash", newPasswordHash);

        int updatedRows = getWritableDatabase().update(
                "users",
                values,
                "email = ?",
                new String[]{normalizeEmail(email)}
        );

        return updatedRows == 1;
    }
}
