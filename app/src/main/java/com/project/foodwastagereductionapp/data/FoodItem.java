package com.project.foodwastagereductionapp.data;

public class FoodItem {
    public final int id;
    public final String name;
    public final double price;
    public final int imageRes;
    public final String description;

    public FoodItem(int id, String name, double price, int imageRes, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.description = description;
    }
}
