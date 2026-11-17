package com.project.foodwastagereductionapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "notifications",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "id",
                        childColumns = "customer_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = OrderEntity.class, parentColumns = "id",
                        childColumns = "order_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("customer_id"), @Index("order_id")}
)
final class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "customer_id")
    long customerId;
    @ColumnInfo(name = "order_id")
    long orderId;
    /** The {@link OrderStatus} the order moved to. */
    String status;
    @ColumnInfo(name = "created_at")
    long createdAt;
}
