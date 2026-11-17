package com.project.foodwastagereductionapp.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {UserEntity.class, RestaurantEntity.class, OrderEntity.class,
                OrderItemEntity.class, NotificationEntity.class},
        version = 4,
        exportSchema = false
)
abstract class AppDatabase extends RoomDatabase {
    abstract UserDao userDao();
    abstract RestaurantDao restaurantDao();
    abstract OrderDao orderDao();
    abstract NotificationDao notificationDao();

    /** Version 3: users get address + phone (Edit Profile). Keeps all existing accounts and restaurants. */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE users ADD COLUMN address TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
        }
    };

    /** Version 4: customer orders, their food lines and notifications. Keeps all existing data. */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`customer_id` INTEGER NOT NULL, `restaurant_id` INTEGER, `restaurant_name` TEXT, "
                    + "`restaurant_address` TEXT, `restaurant_image_uri` TEXT, `placed_at` INTEGER NOT NULL, "
                    + "`item_count` INTEGER NOT NULL, `total` REAL NOT NULL, `status` TEXT, `pickup_time` TEXT, "
                    + "`is_delivery` INTEGER NOT NULL, `delivery_address` TEXT, "
                    + "FOREIGN KEY(`customer_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, "
                    + "FOREIGN KEY(`restaurant_id`) REFERENCES `restaurants`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_customer_id` ON `orders` (`customer_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_restaurant_id` ON `orders` (`restaurant_id`)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `order_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`order_id` INTEGER NOT NULL, `food_item_id` INTEGER NOT NULL, `name` TEXT, "
                    + "`unit_price` REAL NOT NULL, `quantity` INTEGER NOT NULL, "
                    + "FOREIGN KEY(`order_id`) REFERENCES `orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_order_id` ON `order_items` (`order_id`)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`customer_id` INTEGER NOT NULL, `order_id` INTEGER NOT NULL, `status` TEXT, "
                    + "`created_at` INTEGER NOT NULL, "
                    + "FOREIGN KEY(`customer_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, "
                    + "FOREIGN KEY(`order_id`) REFERENCES `orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_customer_id` ON `notifications` (`customer_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_order_id` ON `notifications` (`order_id`)");
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
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                            .fallbackToDestructiveMigration()
                            .build();
                    instance = result;
                }
            }
        }
        return result;
    }
}