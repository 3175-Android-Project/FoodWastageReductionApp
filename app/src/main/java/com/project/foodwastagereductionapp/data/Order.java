package com.project.foodwastagereductionapp.data;

/** One row of the Orders list (read from Room). */
public final class Order {
    public final long id;
    /** Same as the id, as text, for "Order #1025". */
    public final String orderNumber;
    public final String restaurantName;
    public final String date;
    public final String time;
    public final long placedAt;
    public final int itemCount;
    public final double total;
    /** One of the {@link OrderStatus} values. */
    public final String status;
    /** Restaurant photo copied at checkout (may be null or empty). */
    public final String imageUri;

    public Order(long id, String restaurantName, String date, String time, long placedAt,
                 int itemCount, double total, String status, String imageUri) {
        this.id = id;
        this.orderNumber = String.valueOf(id);
        this.restaurantName = restaurantName;
        this.date = date;
        this.time = time;
        this.placedAt = placedAt;
        this.itemCount = itemCount;
        this.total = total;
        this.status = status;
        this.imageUri = imageUri;
    }
}
