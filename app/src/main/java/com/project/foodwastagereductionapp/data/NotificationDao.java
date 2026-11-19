package com.project.foodwastagereductionapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
interface NotificationDao {
    @Insert
    long insert(NotificationEntity notification);

    @Query("SELECT n.id AS id, n.status AS status, o.restaurant_name AS restaurantName, "
            + "o.id AS orderId, o.pickup_time AS pickupTime, n.created_at AS timeMillis, "
            + "o.status AS orderStatus, o.is_delivery AS isDelivery "
            + "FROM notifications n JOIN orders o ON o.id = n.order_id "
            + "WHERE n.customer_id = :customerId ORDER BY n.created_at DESC, n.id DESC")
    List<NotificationRow> findByCustomer(long customerId);
}
