package com.project.foodwastagereductionapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CartDao {
    @Query("SELECT * FROM cart_items WHERE user_id = :userId")
    List<CartItemEntity> findByUser(long userId);

    @Query("SELECT * FROM cart_items WHERE user_id = :userId AND restaurant_id = :restaurantId AND food_item_id = :foodId LIMIT 1")
    CartItemEntity findItem(long userId, long restaurantId, int foodId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CartItemEntity entity);

    @Query("UPDATE cart_items SET quantity = :quantity WHERE user_id = :userId AND restaurant_id = :restaurantId AND food_item_id = :foodId")
    int updateQuantity(long userId, long restaurantId, int foodId, int quantity);

    @Query("DELETE FROM cart_items WHERE user_id = :userId AND restaurant_id = :restaurantId AND food_item_id = :foodId")
    int deleteItem(long userId, long restaurantId, int foodId);

    @Query("DELETE FROM cart_items WHERE user_id = :userId AND restaurant_id = :restaurantId")
    int deleteRestaurantCart(long userId, long restaurantId);

    @Query("DELETE FROM cart_items WHERE user_id = :userId")
    int deleteUserCart(long userId);

    @Query("DELETE FROM cart_items")
    void deleteAll();
}
