package com.project.foodwastagereductionapp.customer;

import android.os.Handler;
import android.os.Looper;

/**
 * Customer Orders feature - DEMO ONLY
 *
 * There is no restaurant app or database yet, so nothing would ever move an order past
 * "Pending". This pretends to be the restaurant for orders placed in the app:
 * Accepted -> Preparing -> Ready, with a notification at each step.
 *
 * Delete this class (and the call in OrderStore.place) once the manager side or the
 * database calls OrderStore.updateStatus itself, or set ENABLED to false to switch it off.
 */
final class OrderStatusSimulator {

    static final boolean ENABLED = true;

    private static final String[] FLOW = {
            OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY
    };
    // Time after the order is placed when each step happens (cancelling is possible until the first)
    private static final long[] DELAYS_MS = {30_000L, 60_000L, 90_000L};

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private OrderStatusSimulator() { }

    static void start(String orderNumber) {
        if (!ENABLED) return;
        for (int i = 0; i < FLOW.length; i++) {
            final String next = FLOW[i];
            HANDLER.postDelayed(() -> advance(orderNumber, next), DELAYS_MS[i]);
        }
    }

    private static void advance(String orderNumber, String next) {
        OrderStore.PlacedOrder placed = OrderStore.find(orderNumber);
        // Stop if the customer cancelled (or the order is otherwise finished)
        if (placed == null || !OrderStatus.isActive(placed.order.status)) return;
        OrderStore.updateStatus(orderNumber, next);
    }
}
