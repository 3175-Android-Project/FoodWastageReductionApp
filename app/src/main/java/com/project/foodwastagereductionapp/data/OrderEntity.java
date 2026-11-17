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
final class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "customer_id")
    long customerId;
    @ColumnInfo(name = "restaurant_id")
    Long restaurantId;
    @ColumnInfo(name = "restaurant_name")
    String restaurantName;
    @ColumnInfo(name = "restaurant_address")
    String restaurantAddress;
    @ColumnInfo(name = "restaurant_image_uri")
    String restaurantImageUri;
    @ColumnInfo(name = "placed_at")
    long placedAt;
    @ColumnInfo(name = "item_count")
    int itemCount;
    double total;
    String status;
    @ColumnInfo(name = "pickup_time")
    String pickupTime;
    @ColumnInfo(name = "is_delivery")
    boolean isDelivery;
    @ColumnInfo(name = "delivery_address")
    String deliveryAddress;
}
