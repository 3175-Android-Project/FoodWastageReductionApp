package com.project.foodwastagereductionapp.data;

public class CartItem {
    public final FoodItem item;
    public int quantity;

    public CartItem(FoodItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public double getLineTotal() {
        if (item == null) return 0.0;
        return item.price * quantity;
    }
}
