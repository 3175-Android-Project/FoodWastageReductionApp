package com.project.foodwastagereductionapp.data;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "food_items")
public class FoodItemEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long restaurantId;

    public String name;

    public String description;

    public String category;

    public double originalPrice;

    public double discountedPrice;

    public int quantityLeft;

    public String availableUntil;

    public boolean available;

    public String imageUri;
}

