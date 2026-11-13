package com.project.foodwastagereductionapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {UserEntity.class, RestaurantEntity.class},
        version = 1,
        exportSchema = false
)
abstract class AppDatabase extends RoomDatabase {
    abstract UserDao userDao();
    abstract RestaurantDao restaurantDao();

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
                            .fallbackToDestructiveMigration()
                            .build();
                    instance = result;
                }
            }
        }
        return result;
    }
}
