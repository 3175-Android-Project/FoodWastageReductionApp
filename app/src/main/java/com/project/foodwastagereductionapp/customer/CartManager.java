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

// In-memory carts for the current session. One cart per restaurant, any number of restaurants.
public final class CartManager {

    // One row in a cart: a food item and how many of it.
    public static final class CartLine {
        public final FoodItem item;
        public final int quantity;

        CartLine(FoodItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }

    // The food in one restaurant's cart.
    private static final class Cart {
        final Map<Integer, FoodItem> items = new LinkedHashMap<>();
        final Map<Integer, Integer> quantities = new LinkedHashMap<>();
    }

    // restaurant id -> its cart (in the order the carts were started)
    private static final Map<Long, Cart> carts = new LinkedHashMap<>();

    private CartManager() {
    }

    // Adds one item to that restaurant's cart.
    public static synchronized void add(long restaurantId, FoodItem item) {
        Cart cart = carts.get(restaurantId);
        if (cart == null) {
            cart = new Cart();
            carts.put(restaurantId, cart);
        }
        cart.items.put(item.id, item);
        Integer current = cart.quantities.get(item.id);
        cart.quantities.put(item.id, current == null ? 1 : current + 1);
    }

    // Removes one. Removes the line when it reaches 0, and the cart when nothing is left.
    public static synchronized void decrease(long restaurantId, int foodId) {
        Cart cart = carts.get(restaurantId);
        if (cart == null) return;
        Integer current = cart.quantities.get(foodId);
        if (current == null) return;
        if (current <= 1) {
            cart.quantities.remove(foodId);
            cart.items.remove(foodId);
        } else {
            cart.quantities.put(foodId, current - 1);
        }
        if (cart.quantities.isEmpty()) carts.remove(restaurantId);
    }

    public static synchronized int quantityOf(long restaurantId, int foodId) {
        Cart cart = carts.get(restaurantId);
        if (cart == null) return 0;
        Integer q = cart.quantities.get(foodId);
        return q == null ? 0 : q;
    }

    // Number of items in one restaurant's cart (sum of quantities).
    public static synchronized int count(long restaurantId) {
        Cart cart = carts.get(restaurantId);
        if (cart == null) return 0;
        int total = 0;
        for (int q : cart.quantities.values()) total += q;
        return total;
    }

    // Number of items in all carts together.
    public static synchronized int count() {
        int total = 0;
        for (long restaurantId : carts.keySet()) total += count(restaurantId);
        return total;
    }

    // Ids of the restaurants that have a cart, in the order the carts were started.
    public static synchronized List<Long> getRestaurantIds() {
        return new ArrayList<>(carts.keySet());
    }

    public static synchronized List<CartLine> getLines(long restaurantId) {
        List<CartLine> lines = new ArrayList<>();
        Cart cart = carts.get(restaurantId);
        if (cart == null) return lines;
        for (Map.Entry<Integer, Integer> e : cart.quantities.entrySet()) {
            lines.add(new CartLine(cart.items.get(e.getKey()), e.getValue()));
        }
        return lines;
    }

    // Sum of discounted price x quantity for one restaurant's cart, before tax.
    public static synchronized double subtotal(long restaurantId) {
        Cart cart = carts.get(restaurantId);
        if (cart == null) return 0;
        double total = 0;
        for (Map.Entry<Integer, Integer> e : cart.quantities.entrySet()) {
            total += cart.items.get(e.getKey()).price * e.getValue();
        }
        return total;
    }

    public static synchronized String earliestAvailableUntil(long restaurantId) {
        Cart cart = carts.get(restaurantId);
        if (cart == null) return null;
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        String earliest = null;
        Date earliestDate = null;
        for (FoodItem f : cart.items.values()) {
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

    // Empties one restaurant's cart (after its order was placed).
    public static synchronized void clear(long restaurantId) {
        carts.remove(restaurantId);
    }

    // Empties every cart (log in / log out).
    public static synchronized void clear() {
        carts.clear();
    }
}
