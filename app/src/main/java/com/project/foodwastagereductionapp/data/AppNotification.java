package com.project.foodwastagereductionapp.data;

/** One entry on the Notifications screen: an order-status update or a pickup reminder. */
public final class AppNotification {
    public final long id;
    /** The {@link OrderStatus} the order changed to (decides the title and icon). */
    public final String status;
    public final String restaurantName;
    public final long orderId;
    public final String pickupTime;
    public final long timeMillis;
    /** The order's current status (decides which screen a tap opens). */
    public final String orderStatus;
    public final boolean isDelivery;

    public AppNotification(long id, String status, String restaurantName, long orderId,
                           String pickupTime, long timeMillis, String orderStatus, boolean isDelivery) {
        this.id = id;
        this.status = status;
        this.restaurantName = restaurantName;
        this.orderId = orderId;
        this.pickupTime = pickupTime;
        this.timeMillis = timeMillis;
        this.orderStatus = orderStatus;
        this.isDelivery = isDelivery;
    }

    public AppNotification(long id, String status, String restaurantName, long orderId,
                           String pickupTime, long timeMillis, String orderStatus) {
        this(id, status, restaurantName, orderId, pickupTime, timeMillis, orderStatus, false);
    }
}
