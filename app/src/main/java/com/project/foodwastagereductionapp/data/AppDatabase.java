package com.project.foodwastagereductionapp.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {UserEntity.class, RestaurantEntity.class},
        version = 3,
        exportSchema = false
)
abstract class AppDatabase extends RoomDatabase {
    abstract UserDao userDao();
    abstract RestaurantDao restaurantDao();

    /** Version 3: users get address + phone (Edit Profile). Keeps all existing accounts and restaurants. */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE users ADD COLUMN address TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
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
                            .addMigrations(MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                    instance = result;
                }
            }
        }
        return result;
    }
}