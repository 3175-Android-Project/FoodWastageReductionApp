package com.project.foodwastagereductionapp.data;

public class Order {
    public final String orderNumber;
    public final String restaurantName;
    public final String date;
    public final String time;
    public final int itemCount;
    public final double total;
    public final int imageRes;

    /** One of the {@link OrderStatus} values. Changes as the restaurant updates the order. */
    public String status;

    public Order(String orderNumber, String restaurantName, String date, String time,
                 int itemCount, double total, String status, int imageRes) {
        this.orderNumber = orderNumber;
        this.restaurantName = restaurantName;
        this.date = date;
        this.time = time;
        this.itemCount = itemCount;
        this.total = total;
        this.status = status;
        this.imageRes = imageRes;
    }
}
