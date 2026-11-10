package com.project.foodwastagereductionapp.customer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Browse & Checkout / Customer Orders feature - CSIS 3175 Intro to Mobile Development
 */
/**
 * In-memory list of the customer's orders (replace with database later).
 * Feeds Order History, Order Detail, Order Status and Notifications.
 *
 * The restaurant side (or the database) should call {@link #updateStatus} whenever an
 * order moves to the next step - the customer screens and notifications update from there.
 */
public final class OrderStore {

    public static class PlacedOrder {
        public final Order order;
        public final List<CartItem> items;
        public final String pickupTime;
        public final String restaurantAddress;

        PlacedOrder(Order order, List<CartItem> items, String pickupTime, String restaurantAddress) {
            this.order = order;
            this.items = items;
            this.pickupTime = pickupTime;
            this.restaurantAddress = restaurantAddress;
        }
    }

    /** Notified (on the main thread) whenever an order's status changes. */
    public interface Listener {
        void onOrdersChanged();
    }

    private static final List<PlacedOrder> ORDERS = new ArrayList<>();
    private static final List<Listener> LISTENERS = new ArrayList<>();
    private static int nextOrderNumber = 1025; // sample orders already use 1022 - 1024

    static {
        seedSampleOrders();
    }

    private OrderStore() { }

    /** Creates a new Pending order from the checkout cart. */
    public static PlacedOrder place(Restaurant restaurant, List<CartItem> cartItems, String pickupTime) {
        List<CartItem> copy = new ArrayList<>(cartItems);
        int itemCount = 0;
        double total = 0;
        for (CartItem c : copy) {
            itemCount += c.quantity;
            total += c.getLineTotal();
        }

        Date now = new Date();
        Order order = new Order(String.valueOf(nextOrderNumber++), restaurant.name,
                formatDate(now), formatTime(now), itemCount, total,
                OrderStatus.PENDING, restaurant.imageRes);
        PlacedOrder placed = new PlacedOrder(order, copy, pickupTime, restaurant.address);
        ORDERS.add(0, placed); // newest first

        OrderStatusSimulator.start(order.orderNumber); // demo only, see that class
        return placed;
    }

    /** All orders, newest first. */
    public static List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        for (PlacedOrder p : ORDERS) orders.add(p.order);
        return orders;
    }

    public static PlacedOrder find(String orderNumber) {
        for (PlacedOrder p : ORDERS) {
            if (p.order.orderNumber.equals(orderNumber)) return p;
        }
        return null;
    }

    /**
     * Moves an order to a new status, adds a notification for the customer
     * and tells any open screens to refresh.
     */
    public static void updateStatus(String orderNumber, String newStatus) {
        PlacedOrder placed = find(orderNumber);
        if (placed == null || placed.order.status.equals(newStatus)) return;

        placed.order.status = newStatus;

        if (shouldNotify(newStatus)) {
            NotificationStore.add(new AppNotification(newStatus, placed.order.restaurantName,
                    orderNumber, placed.pickupTime, System.currentTimeMillis()));
        }
        for (Listener l : new ArrayList<>(LISTENERS)) l.onOrdersChanged();
    }

    public static void addListener(Listener listener) {
        if (!LISTENERS.contains(listener)) LISTENERS.add(listener);
    }

    public static void removeListener(Listener listener) {
        LISTENERS.remove(listener);
    }

    // The customer cancelling their own order does not need a notification
    private static boolean shouldNotify(String status) {
        return OrderStatus.ACCEPTED.equals(status)
                || OrderStatus.PREPARING.equals(status)
                || OrderStatus.READY.equals(status)
                || OrderStatus.REJECTED.equals(status)
                || OrderStatus.COMPLETED.equals(status);
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.US).format(date);
    }

    private static String formatTime(Date date) {
        return new SimpleDateFormat("h:mm a", Locale.US).format(date);
    }

    // ---- Sample data -------------------------------------------------------------------

    private static void seedSampleOrders() {
        Date now = new Date();
        long minute = 60_000L;

        // Current order: accepted, waiting for the food to be ready
        addSample("1022", 1, formatDate(now), formatTime(new Date(now.getTime() - 40 * minute)),
                OrderStatus.ACCEPTED, "6:30 PM", line(1, 101, 1), line(1, 102, 1));
        NotificationStore.add(new AppNotification(OrderStatus.ACCEPTED, "Awesome sushi", "1022",
                "6:30 PM", now.getTime() - 35 * minute));

        // Past orders
        addSample("1024", 1, "Oct 1, 2026", "5:30 PM",
                OrderStatus.COMPLETED, "6:00 PM", line(1, 101, 1));
        addSample("1023", 4, "Oct 1, 2026", "6:10 PM",
                OrderStatus.REJECTED, "6:30 PM", line(4, 401, 1));
    }

    private static CartItem line(int restaurantId, int foodItemId, int quantity) {
        for (FoodItem f : SampleData.getFoodItems(restaurantId)) {
            if (f.id == foodItemId) return new CartItem(f, quantity);
        }
        throw new IllegalArgumentException("Unknown sample food item " + foodItemId);
    }

    private static void addSample(String orderNumber, int restaurantId, String date, String time,
                                  String status, String pickupTime, CartItem... lines) {
        Restaurant restaurant = SampleData.getRestaurant(restaurantId);
        List<CartItem> items = new ArrayList<>();
        int itemCount = 0;
        double total = 0;
        for (CartItem c : lines) {
            items.add(c);
            itemCount += c.quantity;
            total += c.getLineTotal();
        }
        Order order = new Order(orderNumber, restaurant.name, date, time,
                itemCount, total, status, restaurant.imageRes);
        ORDERS.add(new PlacedOrder(order, items, pickupTime, restaurant.address));
    }
}
