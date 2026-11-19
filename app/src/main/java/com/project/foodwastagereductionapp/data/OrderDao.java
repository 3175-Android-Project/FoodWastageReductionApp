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

    @Insert
    void insertItems(List<OrderItemEntity> items);

    @Query("SELECT * FROM orders WHERE customer_id = :customerId ORDER BY placed_at DESC, id DESC")
    List<OrderEntity> findByCustomer(long customerId);

    @Query("SELECT * FROM orders WHERE restaurant_id = :restaurantId ORDER BY placed_at DESC, id DESC")
    List<OrderEntity> findByRestaurant(long restaurantId);

    /** Only returns the order if it belongs to this customer. */
    @Query("SELECT * FROM orders WHERE id = :orderId AND customer_id = :customerId LIMIT 1")
    OrderEntity findForCustomer(long customerId, long orderId);

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    OrderEntity findById(long orderId);

    @Query("SELECT * FROM order_items WHERE order_id = :orderId ORDER BY id")
    List<OrderItemEntity> itemsFor(long orderId);

    /** Changes the status only if it still is {@code expectedStatus}; returns rows changed (0 or 1). */
    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId AND status = :expectedStatus")
    int updateStatus(long orderId, String newStatus, String expectedStatus);

    @Query("SELECT * FROM orders " +
            "WHERE restaurant_id = :restaurantId " +
            "AND (" +
            "status = 'Completed' OR " +
            "status = 'Picked up' OR " +
            "status = 'Delivered' OR " +
            "status = 'Rejected' OR " +
            "status = 'Cancelled')" +
            " ORDER BY placed_at DESC, id DESC"
    )
    List<OrderEntity> getOrderHistory(long restaurantId);

    @Query("SELECT * FROM orders " + "WHERE id = :orderId")
    OrderEntity getOrder(long orderId);

    @Query("SELECT * FROM orders " + "WHERE id = :orderId " + "AND restaurant_id = :restaurantId " + "LIMIT 1")
    OrderEntity findForRestaurant(long restaurantId, long orderId);
}
