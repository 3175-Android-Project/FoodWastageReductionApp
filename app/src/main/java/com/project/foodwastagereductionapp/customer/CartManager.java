package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CustomerProfile;

import java.util.LinkedHashMap;
import java.util.Map;


public final class CartManager {
    private static final Map<Integer, FoodItem> items = new LinkedHashMap<>();
    private static final Map<Integer, Integer> quantities = new LinkedHashMap<>();
    private static long restaurantId = -1;

    private CartManager() {
    }

    /** Returns false if the cart already holds food from a different restaurant. */
    public static synchronized boolean add(long fromRestaurantId, FoodItem item) {
        if (restaurantId != -1 && restaurantId != fromRestaurantId) return false;
        restaurantId = fromRestaurantId;
        items.put(item.id, item);
        Integer current = quantities.get(item.id);
        quantities.put(item.id, current == null ? 1 : current + 1);
        return true;
    }

    public static synchronized int quantityOf(int foodId) {
        Integer q = quantities.get(foodId);
        return q == null ? 0 : q;
    }

    public static synchronized int count() {
        int total = 0;
        for (int q : quantities.values()) total += q;
        return total;
    }

    public static synchronized void clear() {
        items.clear();
        quantities.clear();
        restaurantId = -1;
    }
}