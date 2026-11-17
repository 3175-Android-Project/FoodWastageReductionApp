package com.project.foodwastagereductionapp.data;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insert(OrderEntity order);
    @Update
    void update(OrderEntity order);
    @Query("SELECT * FROM orders " +
                    "WHERE restaurantId = :restaurantId " +
                    "AND (" +
                    "status = 'Picked up' OR " +
                    "status = 'Delivered' OR " +
                    "status = 'Rejected' OR " +
                    "status = 'Cancelled')"
    )
    List<OrderEntity> getOrderHistory(long restaurantId);

    @Query("SELECT * FROM orders " + "WHERE id = :orderId")
    OrderEntity getOrder(long orderId);
}
