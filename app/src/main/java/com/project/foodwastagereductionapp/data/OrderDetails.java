package com.project.foodwastagereductionapp.data;

import java.util.List;

/** Everything the Order Status and Order Detail screens show for one order. */
public final class OrderDetails {
    public final Order order;
    public final List<OrderLine> lines;
    public final String pickupTime;
    public final String restaurantAddress;
    public final boolean isDelivery;
    public final String deliveryAddress;
    public final String customerName;
    public final String customerEmail;
    public final String customerPhone;

    public OrderDetails(Order order, List<OrderLine> lines, String pickupTime,
                        String restaurantAddress, boolean isDelivery, String deliveryAddress,
                        String customerName, String customerEmail, String customerPhone) {
        this.order = order;
        this.lines = lines;
        this.pickupTime = pickupTime;
        this.restaurantAddress = restaurantAddress;
        this.isDelivery = isDelivery;
        this.deliveryAddress = deliveryAddress;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
    }

    public double getSubtotal() {
        double sum = 0;
        for (OrderLine l : lines) sum += l.getLineTotal();
        return sum;
    }
}
