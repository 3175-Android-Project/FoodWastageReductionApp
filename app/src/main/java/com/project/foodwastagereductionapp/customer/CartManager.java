package com.project.foodwastagereductionapp.customer;

import android.content.Context;
import android.os.Looper;

import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.CartItemEntity;
import com.project.foodwastagereductionapp.data.FoodItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// In-memory and Room DB persistent carts for users. One cart per restaurant, any number of restaurants.
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
    private static Context appContext = null;
    private static long activeUserId = -1;

    private CartManager() {
    }

    public static synchronized void init(Context context, long userId) {
        if (context != null) appContext = context.getApplicationContext();
        if (activeUserId != userId) {
            loadForUser(context, userId);
        }
    }

    public static synchronized long getActiveUserId() {
        return activeUserId;
    }

    public static synchronized void saveAndLoadUser(Context context, long userId) {
        if (context != null) appContext = context.getApplicationContext();
        loadForUser(context, userId);
    }

    public static synchronized void saveAndClear(Context context) {
        if (context != null) appContext = context.getApplicationContext();
        carts.clear();
        activeUserId = -1;
    }

    private static void loadForUser(Context context, long userId) {
        activeUserId = userId;
        carts.clear();
        if (context == null || userId == -1) return;

        Runnable task = () -> {
            try {
                AppDatabaseHelper db = AppDatabaseHelper.getInstance(context);
                List<CartItemEntity> rows = db.getCartItemsForUser(userId);
                synchronized (CartManager.class) {
                    if (activeUserId != userId) return;
                    carts.clear();
                    for (CartItemEntity e : rows) {
                        Cart cart = carts.get(e.restaurantId);
                        if (cart == null) {
                            cart = new Cart();
                            carts.put(e.restaurantId, cart);
                        }
                        FoodItem item = new FoodItem(
                                e.foodItemId, e.name, e.originalPrice, e.price,
                                e.quantityLeft, e.availableUntil, e.imageRes,
                                e.description, e.imageUri
                        );
                        cart.items.put(e.foodItemId, item);
                        cart.quantities.put(e.foodItemId, e.quantity);
                    }
                }
            } catch (Exception ignored) {
            }
        };

        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(task).start();
        } else {
            task.run();
        }
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
        int newQuantity = current == null ? 1 : current + 1;
        cart.quantities.put(item.id, newQuantity);

        long userId = activeUserId;
        Context context = appContext;
        if (context != null && userId != -1) {
            new Thread(() -> {
                try {
                    AppDatabaseHelper.getInstance(context).saveCartItem(userId, restaurantId, item, newQuantity);
                } catch (Exception ignored) {
                }
            }).start();
        }
    }

    // Removes one. Removes the line when it reaches 0, and the cart when nothing is left.
    public static synchronized void decrease(long restaurantId, int foodId) {
        Cart cart = carts.get(restaurantId);
        if (cart == null) return;
        Integer current = cart.quantities.get(foodId);
        if (current == null) return;
        FoodItem item = cart.items.get(foodId);
        int newQuantity = current - 1;

        if (newQuantity <= 0) {
            cart.quantities.remove(foodId);
            cart.items.remove(foodId);
        } else {
            cart.quantities.put(foodId, newQuantity);
        }
        if (cart.quantities.isEmpty()) carts.remove(restaurantId);

        long userId = activeUserId;
        Context context = appContext;
        if (context != null && userId != -1) {
            new Thread(() -> {
                try {
                    AppDatabaseHelper db = AppDatabaseHelper.getInstance(context);
                    if (newQuantity > 0 && item != null) {
                        db.saveCartItem(userId, restaurantId, item, newQuantity);
                    } else {
                        db.removeCartItem(userId, restaurantId, foodId);
                    }
                } catch (Exception ignored) {
                }
            }).start();
        }
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
        long userId = activeUserId;
        Context context = appContext;
        if (context != null && userId != -1) {
            new Thread(() -> {
                try {
                    AppDatabaseHelper.getInstance(context).clearCartForRestaurant(userId, restaurantId);
                } catch (Exception ignored) {
                }
            }).start();
        }
    }

    // Empties every cart.
    public static synchronized void clear() {
        carts.clear();
        long userId = activeUserId;
        Context context = appContext;
        if (context != null && userId != -1) {
            new Thread(() -> {
                try {
                    AppDatabaseHelper.getInstance(context).clearCartForUser(userId);
                } catch (Exception ignored) {
                }
            }).start();
        }
    }
}
