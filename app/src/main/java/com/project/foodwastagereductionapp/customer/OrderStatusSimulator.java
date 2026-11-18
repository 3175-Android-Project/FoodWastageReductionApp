package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;

/**
 * Customer Orders feature - DEMO ONLY
 *
 * There is no restaurant (manager) side for orders yet, so nothing would ever move an order
 * past "Pending". This pretends to be the restaurant: every order moves on a fixed schedule
 * after it was placed, with a notification at each step:
 * Accepted -> Preparing -> Ready -> Completed.
 *
 * Because orders are saved in Room, the schedule is worked out from the time the order was
 * placed (not from a timer), so it also works after the app was closed.
 *
 * Delete this class and its calls (the four customer screens that load data) once the manager
 * side calls AppDatabaseHelper.updateOrderStatus itself, or set ENABLED to false to switch it off.
 */
final class OrderStatusSimulator {

    // Off: the restaurant manager now moves orders (Incoming orders screen)
    static final boolean ENABLED = false;

    private static final String[] FLOW = {
            OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED
    };
    // Time after the order was placed when each step happens (the customer can cancel until the first)
    private static final long[] DELAYS_MS = {30_000L, 60_000L, 90_000L, 150_000L};

    /** How often open screens re-read the database to show status changes. */
    static final long POLL_MS = 5_000L;

    private OrderStatusSimulator() { }

    /**
     * Brings this customer's orders up to date. Call on the database worker thread,
     * before reading orders or notifications.
     */
    static void catchUp(AppDatabaseHelper db, long customerId) {
        if (!ENABLED) return;
        long now = System.currentTimeMillis();
        for (Order order : db.getOrders(customerId)) {
            if (!OrderStatus.isActive(order.status)) continue; // finished or cancelled
            long age = now - order.placedAt;
            for (int i = 0; i < FLOW.length; i++) {
                // Moves that are not allowed (already past that step) are ignored by the database
                if (age >= DELAYS_MS[i]) {
                    db.updateOrderStatus(order.id, FLOW[i], order.placedAt + DELAYS_MS[i]);
                }
            }
        }
    }
}
