package com.project.foodwastagereductionapp.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {UserEntity.class, RestaurantEntity.class, FoodItemEntity.class, OrderEntity.class},
        version = 5,
        exportSchema = false
)
abstract class AppDatabase extends RoomDatabase {
    abstract UserDao userDao();
    abstract RestaurantDao restaurantDao();
    abstract FoodItemDao foodItemDao();
    abstract OrderDao orderDao();

    /** Version 3: users get address + phone (Edit Profile). Keeps all existing accounts and restaurants. */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE users ADD COLUMN address TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
        }
    };

    /** Version 4:  managers get to manage food items. */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            db.execSQL(
                    "CREATE TABLE food_items (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "restaurantId INTEGER NOT NULL, " +
                            "name TEXT, " +
                            "description TEXT, " +
                            "category TEXT, " +
                            "originalPrice REAL NOT NULL, " +
                            "discountedPrice REAL NOT NULL, " +
                            "quantityLeft INTEGER NOT NULL, " +
                            "availableUntil TEXT, " +
                            "available INTEGER NOT NULL, " +
                            "imageUri TEXT)"
            );
        }
    };

    /** Version 5: managers can see order history */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase db) {

                    db.execSQL(
                            "CREATE TABLE orders (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            "customerId INTEGER NOT NULL," +
                            "restaurantId INTEGER NOT NULL," +
                            "orderNumber TEXT," +
                            "customerName TEXT," +
                            "foodItems TEXT," +
                            "date TEXT," +
                            "time TEXT," +
                            "itemCount INTEGER NOT NULL," +
                            "total REAL NOT NULL," +
                            "status TEXT," +
                            "pickupMethod TEXT)"
                    );
                }
    };

    private static volatile AppDatabase instance;

    static AppDatabase getInstance(Context context, String databaseName) {
        AppDatabase result = instance;
        if (result == null || !result.isOpen()
                || !databaseName.equals(result.getOpenHelper().getDatabaseName())) {
            synchronized (AppDatabase.class) {
                result = instance;
                if (result == null || !result.isOpen()
                        || !databaseName.equals(result.getOpenHelper().getDatabaseName())) {
                    if (result != null) result.close();
                    result = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    databaseName
                            )
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .fallbackToDestructiveMigration()
                            .build();
                    instance = result;
                }
            }
        }
        return result;
    }
}