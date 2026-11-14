package com.project.foodwastagereductionapp.data;

public class FoodItem {
    public final int id;
    public final String name;
    public final double price;
    public final double originalPrice;
    public final int quantityLeft;
    public final int imageRes;
    public final String description;

    public FoodItem(int id, String name, double originalPrice, double price,
                    int quantityLeft, int imageRes, String description) {
        this.id = id;
        this.name = name;
        this.originalPrice = originalPrice;
        this.price = price;
        this.quantityLeft = quantityLeft;
        this.imageRes = imageRes;
        this.description = description;
    }

    /** Old constructor kept so existing code still compiles (no discount, quantity unknown). */
    public FoodItem(int id, String name, double price, int imageRes, String description) {
        this(id, name, price, price, 0, imageRes, description);
    }
}
