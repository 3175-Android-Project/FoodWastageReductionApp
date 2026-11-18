package com.project.foodwastagereductionapp.data;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface FoodItemDao {
    @Insert
    long insert(FoodItemEntity item);

    @Update
    void update(FoodItemEntity item);

    @Delete
    void delete(FoodItemEntity item);

    @Query(
            "SELECT * FROM food_items " +
                    "WHERE restaurantId = :restaurantId"
    )
    List<FoodItemEntity> getRestaurantFoodItems(
            long restaurantId
    );

    @Query(
            "SELECT * FROM food_items " +
                    "WHERE id = :foodItemId"
    )
    FoodItemEntity getFoodItem(
            long foodItemId
    );

    // Customer side: only items the manager has switched on
    @Query("SELECT * FROM food_items WHERE restaurantId = :restaurantId AND available = 1 ORDER BY id")
    List<FoodItemEntity> getAvailableForRestaurant(long restaurantId);

    // Customer side: one item, only if it belongs to this restaurant and is switched on
    @Query("SELECT * FROM food_items WHERE id = :foodItemId AND restaurantId = :restaurantId AND available = 1 LIMIT 1")
    FoodItemEntity getAvailableFoodItem(long restaurantId, long foodItemId);

    /** Takes stock only if enough is left; returns rows changed (0 = not enough stock). */
    @Query("UPDATE food_items SET quantityLeft = quantityLeft - :quantity "
            + "WHERE id = :foodItemId AND restaurantId = :restaurantId AND available = 1 "
            + "AND quantityLeft >= :quantity")
    int decreaseStock(long restaurantId, long foodItemId, int quantity);

    /** Puts stock back (order rejected or cancelled). */
    @Query("UPDATE food_items SET quantityLeft = quantityLeft + :quantity WHERE id = :foodItemId")
    int increaseStock(long foodItemId, int quantity);
}
