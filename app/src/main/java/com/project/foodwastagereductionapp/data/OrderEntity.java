package com.project.foodwastagereductionapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A customer's order. Restaurant name, address and photo are copied here so the history
 * stays correct even if the restaurant later edits or removes its profile.
 */
@Entity(
        tableName = "orders",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "id",
                        childColumns = "customer_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = RestaurantEntity.class, parentColumns = "id",
                        childColumns = "restaurant_id", onDelete = ForeignKey.SET_NULL)
        },
        indices = {@Index("customer_id"), @Index("restaurant_id")}
)
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "customer_id")
    public long customerId;
    @ColumnInfo(name = "restaurant_id")
    public Long restaurantId;
    @ColumnInfo(name = "restaurant_name")
    public String restaurantName;
    @ColumnInfo(name = "restaurant_address")
    public String restaurantAddress;
    @ColumnInfo(name = "restaurant_image_uri")
    public String restaurantImageUri;
    @ColumnInfo(name = "placed_at")
    public long placedAt;
    @ColumnInfo(name = "item_count")
    public int itemCount;
    public double total;
    public String status;
    @ColumnInfo(name = "pickup_time")
    public String pickupTime;
    @ColumnInfo(name = "is_delivery")
    public boolean isDelivery;
    @ColumnInfo(name = "delivery_address")
    public String deliveryAddress;
}
