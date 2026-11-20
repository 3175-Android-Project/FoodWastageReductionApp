package com.project.foodwastagereductionapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "cart_items",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
public class CartItemEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "user_id")
    public long userId;

    @ColumnInfo(name = "restaurant_id")
    public long restaurantId;

    @ColumnInfo(name = "food_item_id")
    public int foodItemId;

    @ColumnInfo(name = "quantity")
    public int quantity;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "price")
    public double price;

    @ColumnInfo(name = "original_price")
    public double originalPrice;

    @ColumnInfo(name = "quantity_left")
    public int quantityLeft;

    @ColumnInfo(name = "available_until")
    public String availableUntil;

    @ColumnInfo(name = "image_res")
    public int imageRes;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "image_uri")
    public String imageUri;
}
