package com.project.foodwastagereductionapp.data;

public class FoodItem {
    public final int id;
    public final String name;
    public final double price;
    public final double originalPrice;
    public final int quantityLeft;
    public final String availableUntil;
    public final int imageRes;
    public final String description;
    public final String imageUri; // photo the manager uploaded (may be null/empty)

    public FoodItem(int id, String name, double originalPrice, double price,
                    int quantityLeft, String availableUntil, int imageRes, String description,
                    String imageUri) {
        this.id = id;
        this.name = name;
        this.originalPrice = originalPrice;
        this.price = price;
        this.quantityLeft = quantityLeft;
        this.availableUntil = availableUntil;
        this.imageRes = imageRes;
        this.description = description;
        this.imageUri = imageUri;
    }

    // Without a photo.
    public FoodItem(int id, String name, double originalPrice, double price,
                    int quantityLeft, String availableUntil, int imageRes, String description) {
        this(id, name, originalPrice, price, quantityLeft, availableUntil, imageRes, description, null);
    }

    // Without a pickup time.
    public FoodItem(int id, String name, double originalPrice, double price,
                    int quantityLeft, int imageRes, String description) {
        this(id, name, originalPrice, price, quantityLeft, null, imageRes, description, null);
    }

    // Old constructor kept so existing code still compiles (no discount, quantity unknown).
    public FoodItem(int id, String name, double price, int imageRes, String description) {
        this(id, name, price, price, 0, null, imageRes, description, null);
    }

    public boolean hasImageUri() {
        return imageUri != null && !imageUri.isEmpty();
    }
}