package com.project.foodwastagereductionapp.data;

/** Result of the notifications + orders join; becomes an {@link AppNotification}. */
final class NotificationRow {
    long id;
    String status;
    String restaurantName;
    long orderId;
    String pickupTime;
    long timeMillis;
    String orderStatus;
    boolean isDelivery;
}
