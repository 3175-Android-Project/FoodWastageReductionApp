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
}
