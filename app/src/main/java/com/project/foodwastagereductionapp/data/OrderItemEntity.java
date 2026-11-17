package com.project.foodwastagereductionapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "order_items",
        foreignKeys = @ForeignKey(entity = OrderEntity.class, parentColumns = "id",
                childColumns = "order_id", onDelete = ForeignKey.CASCADE),
        indices = {@Index("order_id")}
)
final class OrderItemEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "order_id")
    long orderId;
    @ColumnInfo(name = "food_item_id")
    int foodItemId;
    String name;
    @ColumnInfo(name = "unit_price")
    double unitPrice;
    int quantity;
}
