package com.project.foodwastagereductionapp.customer;

public class Order {
    public final String orderNumber;
    public final String restaurantName;
    public final String date;
    public final int itemCount;
    public final double total;
    public final String status;   // "Completed", "Rejected", "Pending"
    public final int imageRes;

    public Order(String orderNumber, String restaurantName, String date,
                 int itemCount, double total, String status, int imageRes) {
        this.orderNumber = orderNumber;
        this.restaurantName = restaurantName;
        this.date = date;
        this.itemCount = itemCount;
        this.total = total;
        this.status = status;
        this.imageRes = imageRes;
    }
}