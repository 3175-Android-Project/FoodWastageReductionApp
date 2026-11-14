package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// In-memory cart for the current session. One restaurant per cart.
public final class CartManager {

    // One row in the cart: a food item and how many of it.
    public static final class CartLine {
        public final FoodItem item;
        public final int quantity;

        CartLine(FoodItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }

    private static final Map<Integer, FoodItem> items = new LinkedHashMap<>();
    private static final Map<Integer, Integer> quantities = new LinkedHashMap<>();
    private static long restaurantId = -1;

    private CartManager() {
    }

    // Adds one. Returns false if the cart already holds food from a different restaurant.
    public static synchronized boolean add(long fromRestaurantId, FoodItem item) {
        if (restaurantId != -1 && restaurantId != fromRestaurantId) return false;
        restaurantId = fromRestaurantId;
        items.put(item.id, item);
        Integer current = quantities.get(item.id);
        quantities.put(item.id, current == null ? 1 : current + 1);
        return true;
    }

    // Removes one. Removes the line when it reaches 0; empties the cart's restaurant when nothing is left.
    public static synchronized void decrease(int foodId) {
        Integer current = quantities.get(foodId);
        if (current == null) return;
        if (current <= 1) {
            quantities.remove(foodId);
            items.remove(foodId);
        } else {
            quantities.put(foodId, current - 1);
        }
        if (quantities.isEmpty()) restaurantId = -1;
    }

    public static synchronized int quantityOf(int foodId) {
        Integer q = quantities.get(foodId);
        return q == null ? 0 : q;
    }

    // Total number of items (sum of quantities).
    public static synchronized int count() {
        int total = 0;
        for (int q : quantities.values()) total += q;
        return total;
    }

    public static synchronized List<CartLine> getLines() {
        List<CartLine> lines = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : quantities.entrySet()) {
            lines.add(new CartLine(items.get(e.getKey()), e.getValue()));
        }
        return lines;
    }

    // Sum of discounted price x quantity, before tax.
    public static synchronized double subtotal() {
        double total = 0;
        for (Map.Entry<Integer, Integer> e : quantities.entrySet()) {
            total += items.get(e.getKey()).price * e.getValue();
        }
        return total;
    }

    public static synchronized long getRestaurantId() {
        return restaurantId;
    }

    public static synchronized String earliestAvailableUntil() {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        String earliest = null;
        Date earliestDate = null;
        for (FoodItem f : items.values()) {
            if (f.availableUntil == null || f.availableUntil.isEmpty()) continue;
            try {
                Date d = format.parse(f.availableUntil);
                if (earliestDate == null || d.before(earliestDate)) {
                    earliestDate = d;
                    earliest = f.availableUntil;
                }
            } catch (ParseException ignored) {
                // Unknown time format: skip it
            }
        }
        return earliest;
    }

    public static synchronized void clear() {
        items.clear();
        quantities.clear();
        restaurantId = -1;
    }
}