package com.project.foodwastagereductionapp.customer;

/**
 * Customer Orders feature
 *
 * One entry on the Notifications screen: an order-status update or a pickup reminder.
 */
public class AppNotification {
    /** The {@link OrderStatus} the order changed to (decides the title and icon). */
    public final String status;
    public final String restaurantName;
    public final String orderNumber;
    public final String pickupTime;
    public final long timeMillis;

    public AppNotification(String status, String restaurantName, String orderNumber,
                           String pickupTime, long timeMillis) {
        this.status = status;
        this.restaurantName = restaurantName;
        this.orderNumber = orderNumber;
        this.pickupTime = pickupTime;
        this.timeMillis = timeMillis;
    }
}
