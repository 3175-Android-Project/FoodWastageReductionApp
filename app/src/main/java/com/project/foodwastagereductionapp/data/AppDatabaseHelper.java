package com.project.foodwastagereductionapp.data;

import android.content.Context;
import android.util.Patterns;

import java.util.Locale;

/**
 * Application data facade backed by Room.
 * Call methods from a worker thread, not the main UI thread.
 */
public final class AppDatabaseHelper {
    public static final String DATABASE_NAME = "grabbit.db";

    private final AppDatabase database;

    public static synchronized AppDatabaseHelper getInstance(Context context) {
        return new AppDatabaseHelper(context, DATABASE_NAME);
    }

    public AppDatabaseHelper(Context context, String databaseName) {
        database = AppDatabase.getInstance(context, databaseName);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean validEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(normalizeEmail(email)).matches();
    }

    public boolean emailExists(String email) {
        return database.userDao().findByEmail(normalizeEmail(email)) != null;
    }

    /** Hash is generated before entering this method; plaintext passwords are never stored. */
    public long createUser(String email, String passwordHash, String role, String fullName,
                           String city, String province, String postalCode) {
        if (!validEmail(email) || passwordHash == null || !passwordHash.startsWith("pbkdf2_sha1$")
                || !(User.CUSTOMER.equals(role) || User.MANAGER.equals(role))) {
            throw new IllegalArgumentException("Invalid signup details");
        }
        UserEntity user = new UserEntity();
        user.email = normalizeEmail(email);
        user.passwordHash = passwordHash;
        user.role = role;
        user.fullName = required(fullName);
        user.city = required(city);
        user.province = required(province);
        user.postalCode = required(postalCode);
        long id = database.userDao().insert(user);
        if (id == -1) throw new DuplicateEmailException();
        return id;
    }

    /** Also returns pending accounts so login can resume interrupted verification/onboarding. */
    public User authenticate(String email, String password) {
        UserEntity user = database.userDao().findByEmail(normalizeEmail(email));
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) return null;
        return toUser(user);
    }

    public User getUser(long id) {
        return toUser(database.userDao().findById(id));
    }

    public User confirmEmailLocally(long userId) {
        if (database.userDao().confirmEmail(userId) != 1) {
            throw new IllegalArgumentException("Account no longer exists");
        }
        return getUser(userId);
    }

    /** Idempotent save: revisiting restaurant setup updates the same row. */
    public void saveRestaurant(Restaurant restaurant) {
        if (restaurant == null) throw new IllegalArgumentException("Restaurant is required");
        database.runInTransaction(() -> {
            User owner = getUser(restaurant.managerId);
            if (owner == null || !User.MANAGER.equals(owner.role) || !owner.emailVerified) {
                throw new IllegalArgumentException("A confirmed manager account is required");
            }
            String name = required(restaurant.name);
            String city = required(restaurant.city);
            String province = required(restaurant.province);
            String postalCode = required(restaurant.postalCode);
            String phone = required(restaurant.phone);
            String cuisineType = required(restaurant.cuisineType);
            String description = restaurant.description == null ? "" : restaurant.description.trim();
            String imageUri = restaurant.imageUri == null ? "" : restaurant.imageUri;

            RestaurantDao restaurants = database.restaurantDao();
            if (restaurants.update(restaurant.managerId, name, city, province, postalCode,
                    phone, cuisineType, description, imageUri) == 0) {
                RestaurantEntity entity = new RestaurantEntity();
                entity.managerId = restaurant.managerId;
                entity.name = name;
                entity.city = city;
                entity.province = province;
                entity.postalCode = postalCode;
                entity.phone = phone;
                entity.cuisineType = cuisineType;
                entity.description = description;
                entity.imageUri = imageUri;
                restaurants.insert(entity);
            }
        });
    }

    public Restaurant getRestaurantForManager(long managerId) {
        RestaurantEntity restaurant = database.restaurantDao().findByManagerId(managerId);
        if (restaurant == null) return null;
        return new Restaurant(restaurant.id, restaurant.managerId, restaurant.name, restaurant.city,
                restaurant.province, restaurant.postalCode, restaurant.phone,
                restaurant.cuisineType, restaurant.description, restaurant.imageUri);
    }

    public boolean updatePasswordByEmail(String email, String newPasswordHash) {
        if (!validEmail(email) || newPasswordHash == null
                || !newPasswordHash.startsWith("pbkdf2_sha1$")) {
            throw new IllegalArgumentException("Invalid password reset details");
        }
        return database.userDao().updatePassword(normalizeEmail(email), newPasswordHash) == 1;
    }

    public void deleteUser(long userId) {
        database.userDao().deleteById(userId);
    }

    public int countRestaurants() {
        return database.restaurantDao().count();
    }

    public void close() {
        database.close();
    }

    private static User toUser(UserEntity user) {
        if (user == null) return null;
        return new User(user.id, user.email, user.role, user.fullName, user.city,
                user.province, user.postalCode, user.emailVerified);
    }

    private static String required(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Required field");
        return value.trim();
    }

    public static final class DuplicateEmailException extends IllegalArgumentException {
        public DuplicateEmailException() {
            super("Email already registered");
        }
    }
}
