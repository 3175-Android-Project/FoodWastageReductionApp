package com.project.foodwastagereductionapp.data;

/** One food line of an order: name and price are copies from checkout time. */
public final class OrderLine {
    public final int foodItemId;
    public final String name;
    public final double unitPrice;
    public final int quantity;

    public OrderLine(int foodItemId, String name, double unitPrice, int quantity) {
        this.foodItemId = foodItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return unitPrice * quantity;
    }
}
