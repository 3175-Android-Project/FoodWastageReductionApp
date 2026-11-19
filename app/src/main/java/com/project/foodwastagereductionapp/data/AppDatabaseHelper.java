package com.project.foodwastagereductionapp.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Patterns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Application data facade backed by Room.
 * Call methods from a worker thread, not the main UI thread.
 */
public final class AppDatabaseHelper {
    public static final String DATABASE_NAME = "grabbit.db";

    private final AppDatabase database;

    public static synchronized AppDatabaseHelper getInstance(Context context) {
        return new AppDatabaseHelper(context, DATABASE_NAME);
    }

    public AppDatabaseHelper(Context context, String databaseName) {
        database = AppDatabase.getInstance(context, databaseName);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean validEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(normalizeEmail(email)).matches();
    }

    public boolean emailExists(String email) {
        return database.userDao().findByEmail(normalizeEmail(email)) != null;
    }

    /** Hash is generated before entering this method; plaintext passwords are never stored. */
    public long createUser(String email, String passwordHash, String role, String fullName,
                           String city, String province, String postalCode) {
        return createUser(email, passwordHash, role, fullName, city, province, postalCode, "", "");
    }

    public long createUser(String email, String passwordHash, String role, String fullName,
                           String city, String province, String postalCode, String address,
                           String photoUri) {
        if (!validEmail(email) || passwordHash == null || !passwordHash.startsWith("pbkdf2_sha1$")
                || !(User.CUSTOMER.equals(role) || User.MANAGER.equals(role))) {
            throw new IllegalArgumentException("Invalid signup details");
        }
        UserEntity user = new UserEntity();
        user.email = normalizeEmail(email);
        user.passwordHash = passwordHash;
        user.role = role;
        user.fullName = required(fullName);
        user.city = required(city);
        user.province = required(province);
        user.postalCode = required(postalCode);
        user.address = address == null ? "" : address.trim();
        user.profilePhotoUri = photoUri == null ? "" : photoUri;
        long id = database.userDao().insert(user);
        if (id == -1) throw new DuplicateEmailException();
        return id;
    }

    /** Also returns pending accounts so login can resume interrupted verification/onboarding. */
    public User authenticate(String email, String password) {
        UserEntity user = database.userDao().findByEmail(normalizeEmail(email));
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) return null;
        return toUser(user);
    }

    public User getUser(long id) {
        return toUser(database.userDao().findById(id));
    }

    public User confirmEmailLocally(long userId) {
        if (database.userDao().confirmEmail(userId) != 1) {
            throw new IllegalArgumentException("Account no longer exists");
        }
        return getUser(userId);
    }

    /** Idempotent save: revisiting restaurant setup updates the same row. */
    public void saveRestaurant(Restaurant restaurant) {
        if (restaurant == null) throw new IllegalArgumentException("Restaurant is required");
        database.runInTransaction(() -> {
            User owner = getUser(restaurant.managerId);
            if (owner == null || !User.MANAGER.equals(owner.role) || !owner.emailVerified) {
                throw new IllegalArgumentException("A confirmed manager account is required");
            }
            String name = required(restaurant.name);
            String city = required(restaurant.city);
            String province = required(restaurant.province);
            String postalCode = required(restaurant.postalCode);
            String phone = required(restaurant.phone);
            String cuisineType = required(restaurant.cuisineType);
            String description = restaurant.description == null ? "" : restaurant.description.trim();
            String imageUri = restaurant.imageUri == null ? "" : restaurant.imageUri;

            RestaurantDao restaurants = database.restaurantDao();
            if (restaurants.update(restaurant.managerId, name, city, province, postalCode,
                    phone, cuisineType, description, imageUri) == 0) {
                RestaurantEntity entity = new RestaurantEntity();
                entity.managerId = restaurant.managerId;
                entity.name = name;
                entity.city = city;
                entity.province = province;
                entity.postalCode = postalCode;
                entity.phone = phone;
                entity.cuisineType = cuisineType;
                entity.description = description;
                entity.imageUri = imageUri;
                restaurants.insert(entity);
            }
        });
    }

    public Restaurant getRestaurantForManager(long managerId) {
        RestaurantEntity restaurant = database.restaurantDao().findByManagerId(managerId);
        if (restaurant == null) return null;
        return new Restaurant(restaurant.id, restaurant.managerId, restaurant.name, restaurant.city,
                restaurant.province, restaurant.postalCode, restaurant.phone,
                restaurant.cuisineType, restaurant.description, restaurant.imageUri);
    }
    public Restaurant getRestaurant(long id) {
        RestaurantEntity e = database.restaurantDao().findById(id);
        if (e == null) return null;
        return new Restaurant(e.id, e.managerId, e.name, e.city, e.province,
                e.postalCode, e.phone, e.cuisineType, e.description, e.imageUri);
    }
    /** Called only after the email service has verified a reset code for this account. */
    public String issuePasswordResetGrant(
            com.project.foodwastagereductionapp.auth.PasswordResetVerification.VerifiedEmail proof) {
        if (proof == null) throw new IllegalArgumentException("Email verification is required");
        String email = proof.getEmail();
        String token = ResetTokens.create();
        long now = System.currentTimeMillis();
        database.runInTransaction(() -> {
            UserEntity user = database.userDao().findByEmail(normalizeEmail(email));
            if (user == null) throw new IllegalArgumentException("Account not found");
            PasswordResetGrantDao grants = database.passwordResetGrantDao();
            grants.deleteExpired(now);
            grants.deleteForUser(user.id);
            PasswordResetGrantEntity grant = new PasswordResetGrantEntity();
            grant.tokenHash = ResetTokens.hash(token);
            grant.userId = user.id;
            grant.issuedAt = now;
            grant.expiresAt = now + 5 * 60 * 1000L;
            grants.insert(grant);
        });
        return token;
    }

    public boolean isPasswordResetGrantValid(String email, String token) {
        String hash = ResetTokens.hash(token);
        UserEntity user = database.userDao().findByEmail(normalizeEmail(email));
        return hash != null && user != null && database.passwordResetGrantDao()
                .findValid(hash, user.id, System.currentTimeMillis()) != null;
    }

    /** Password update and token consumption either both commit or both roll back. */
    public boolean resetPassword(String email, String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Use at least 6 characters");
        }
        String hash = ResetTokens.hash(token);
        if (hash == null) return false;
        String passwordHash = PasswordHasher.hash(newPassword);
        final boolean[] updated = {false};
        database.runInTransaction(() -> {
            UserEntity user = database.userDao().findByEmail(normalizeEmail(email));
            if (user == null || database.passwordResetGrantDao()
                    .findValid(hash, user.id, System.currentTimeMillis()) == null) return;
            updated[0] = database.userDao().updatePassword(user.id, passwordHash) == 1;
            if (updated[0]) database.passwordResetGrantDao().deleteForUser(user.id);
        });
        return updated[0];
    }

    /** Changing a signed-in user's password also requires their current password. */
    public boolean changePassword(long userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Use at least 6 characters");
        }
        String hash = PasswordHasher.hash(newPassword);
        final boolean[] updated = {false};
        database.runInTransaction(() -> {
            UserEntity user = database.userDao().findById(userId);
            if (user == null || !user.emailVerified
                    || !PasswordHasher.verify(currentPassword, user.passwordHash)) return;
            updated[0] = database.userDao().updatePassword(userId, hash) == 1;
            if (updated[0]) database.passwordResetGrantDao().deleteForUser(userId);
        });
        return updated[0];
    }

    public void deleteUser(long userId) {
        database.userDao().deleteById(userId);
    }

    // Customer search: matches restaurant name, city, province or cuisine type. Empty query returns all.
    public java.util.List<RestaurantListing> searchRestaurants(String query) {
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        java.util.List<RestaurantListing> results = new java.util.ArrayList<>();
        for (RestaurantEntity e : database.restaurantDao().search(like)) {
            Restaurant r = new Restaurant(e.id, e.managerId, e.name, e.city, e.province,
                    e.postalCode, e.phone, e.cuisineType, e.description, e.imageUri);
            results.add(new RestaurantListing(r, 0, null));
        }
        return results;
    }

    public int countRestaurants() {
        return database.restaurantDao().count();
    }


    // ---------------------------------------------------------------- Customer orders

    /**
     * Saves a new Pending order with its food lines. Restaurant details and item names/prices are
     * copied so the order history never changes later. The total includes tax.
     *
     * @return the new order id
     */
    public long placeOrder(long customerId, long restaurantId, List<OrderLine> lines,
                           String pickupTime, boolean isDelivery, String deliveryAddress) {
        return placeOrder(customerId, restaurantId, lines, pickupTime, isDelivery,
                deliveryAddress, System.currentTimeMillis());
    }

    public long placeOrder(long customerId, long restaurantId, List<OrderLine> lines,
                           String pickupTime, boolean isDelivery, String deliveryAddress,
                           long placedAt) {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Order is empty");
        final long[] orderId = new long[1];
        database.runInTransaction(() -> {
            User customer = getUser(customerId);
            if (customer == null || !User.CUSTOMER.equals(customer.role)) {
                throw new IllegalArgumentException("A customer account is required");
            }
            RestaurantEntity restaurant = database.restaurantDao().findById(restaurantId);
            if (restaurant == null) throw new IllegalArgumentException("Restaurant no longer exists");
            if (isDelivery && (deliveryAddress == null || deliveryAddress.trim().isEmpty())) {
                throw new IllegalArgumentException("Delivery address is required");
            }

            int itemCount = 0;
            double subtotal = 0;
            for (OrderLine line : lines) {
                if (line.quantity < 1) throw new IllegalArgumentException("Invalid quantity");
                itemCount += line.quantity;
                subtotal += line.getLineTotal();
            }

            // Take the stock. If any item no longer has enough left, the whole order is rolled back.
            for (OrderLine line : lines) {
                if (database.foodItemDao().decreaseStock(restaurant.id, line.foodItemId, line.quantity) != 1) {
                    throw new OutOfStockException(line.name);
                }
            }

            OrderEntity order = new OrderEntity();
            order.customerId = customerId;
            order.restaurantId = restaurant.id;
            order.restaurantName = restaurant.name;
            order.restaurantAddress = restaurant.city + ", " + restaurant.province;
            order.restaurantImageUri = restaurant.imageUri;
            order.placedAt = placedAt;
            order.itemCount = itemCount;
            order.total = OrderPricing.totalWithTax(subtotal);
            order.status = OrderStatus.PENDING;
            order.pickupTime = pickupTime;
            order.isDelivery = isDelivery;
            order.deliveryAddress = isDelivery ? deliveryAddress.trim() : null;
            long id = database.orderDao().insert(order);

            List<OrderItemEntity> items = new ArrayList<>();
            for (OrderLine line : lines) {
                OrderItemEntity item = new OrderItemEntity();
                item.orderId = id;
                item.foodItemId = line.foodItemId;
                item.name = line.name;
                item.unitPrice = line.unitPrice;
                item.quantity = line.quantity;
                items.add(item);
            }
            database.orderDao().insertItems(items);
            orderId[0] = id;
        });
        return orderId[0];
    }

    /** All of this customer's orders, newest first. */
    public List<Order> getOrders(long customerId) {
        List<Order> result = new ArrayList<>();
        for (OrderEntity e : database.orderDao().findByCustomer(customerId)) result.add(toOrder(e));
        return result;
    }

    /** All of this restaurant's orders, newest first. */
    public List<Order> getOrdersForRestaurant(long restaurantId) {
        List<Order> result = new ArrayList<>();
        for (OrderEntity e : database.orderDao().findByRestaurant(restaurantId)) {
            result.add(toOrder(e));
        }
        return result;
    }

    public List<OrderEntity> getOrderEntitiesForRestaurant(long restaurantId) {
        return database.orderDao().findByRestaurant(restaurantId);
    }

    /** Null if the order does not exist or belongs to someone else. */
    public OrderDetails getOrderDetails(long customerId, long orderId) {
        OrderEntity e = database.orderDao().findForCustomer(customerId, orderId);
        if (e == null) return null;
        List<OrderLine> lines = new ArrayList<>();
        for (OrderItemEntity i : database.orderDao().itemsFor(orderId)) {
            lines.add(new OrderLine(i.foodItemId, i.name, i.unitPrice, i.quantity));
        }
        User customer = getUser(customerId);
        return new OrderDetails(toOrder(e), lines, e.pickupTime, e.restaurantAddress,
                e.isDelivery, e.deliveryAddress,
                customer == null ? "" : customer.fullName,
                customer == null ? "" : customer.email,
                customer == null || customer.phone == null ? "" : customer.phone);
    }

    /** Customers can only cancel their own order while it is still Pending. */
    public boolean cancelOrder(long customerId, long orderId) {
        final boolean[] cancelled = new boolean[1];
        database.runInTransaction(() -> {
            OrderEntity e = database.orderDao().findForCustomer(customerId, orderId);
            if (e == null || !OrderStatus.canCancel(e.status)) return;
            cancelled[0] = database.orderDao()
                    .updateStatus(orderId, OrderStatus.CANCELLED, e.status) == 1;
            if (cancelled[0]) restoreStock(orderId); // the food is available again
        });
        return cancelled[0];
    }

    /**
     * Moves an order to its next status (restaurant side or the demo simulator) and adds a
     * notification for the customer. Moves that skip steps or leave a finished order are ignored.
     *
     * @return true if the status changed
     */
    public boolean updateOrderStatus(long orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, System.currentTimeMillis());
    }

    public boolean updateOrderStatus(long orderId, String newStatus, long atMillis) {
        final boolean[] changed = new boolean[1];
        database.runInTransaction(() -> {
            OrderEntity e = database.orderDao().findById(orderId);
            if (e == null || !OrderStatus.canMoveTo(e.status, newStatus)) return;
            if (database.orderDao().updateStatus(orderId, newStatus, e.status) != 1) return;
            if (OrderStatus.REJECTED.equals(newStatus)) restoreStock(orderId); // food available again
            if (OrderStatus.shouldNotify(newStatus)) {
                NotificationEntity n = new NotificationEntity();
                n.customerId = e.customerId;
                n.orderId = orderId;
                n.status = newStatus;
                n.createdAt = atMillis;
                database.notificationDao().insert(n);
            }
            changed[0] = true;
        });
        return changed[0];
    }
    // ---- Restaurant (manager) side: process incoming orders --------------------------------

    /**
     * Manager moves one of their own restaurant's orders to the next status
     * (Accept, Reject, Start preparing, Mark ready, Picked up / Delivered = Completed).
     * Orders of other restaurants and moves that skip steps are ignored.
     *
     * @return true if the status changed
     */
    public boolean updateOrderStatusForRestaurant(long managerId, long orderId, String newStatus) {
        if (!isOrderOfManager(managerId, orderId)) return false;
        return updateOrderStatus(orderId, newStatus);
    }

    /**
     * Manager reminds the customer to collect a Ready order: adds a "Your order is ready"
     * notification to the customer's bell.
     *
     * @return true if the reminder was added
     */
    public boolean sendPickupReminder(long managerId, long orderId) {
        if (!isOrderOfManager(managerId, orderId)) return false;
        OrderEntity e = database.orderDao().findById(orderId);
        if (e == null || !OrderStatus.READY.equals(e.status)) return false;
        NotificationEntity n = new NotificationEntity();
        n.customerId = e.customerId;
        n.orderId = orderId;
        n.status = OrderStatus.READY;
        n.createdAt = System.currentTimeMillis();
        database.notificationDao().insert(n);
        return true;
    }

    private boolean isOrderOfManager(long managerId, long orderId) {
        RestaurantEntity restaurant = database.restaurantDao().findByManagerId(managerId);
        OrderEntity e = database.orderDao().findById(orderId);
        return restaurant != null && e != null && e.restaurantId != null
                && e.restaurantId == restaurant.id;
    }

    /** Gives the order's food back to the restaurant's stock (rejected / cancelled orders). */
    private void restoreStock(long orderId) {
        for (OrderItemEntity item : database.orderDao().itemsFor(orderId)) {
            database.foodItemDao().increaseStock(item.foodItemId, item.quantity);
        }
    }

    /** Order-status updates and pickup reminders, newest first. */
    public List<AppNotification> getNotifications(long customerId) {
        List<AppNotification> result = new ArrayList<>();
        for (NotificationRow r : database.notificationDao().findByCustomer(customerId)) {
            result.add(new AppNotification(r.id, r.status, r.restaurantName, r.orderId,
                    r.pickupTime, r.timeMillis, r.orderStatus, r.isDelivery));
        }
        return result;
    }

    public List<AppNotification> getManagerNotifications(long managerId) {
        List<AppNotification> result = new ArrayList<>();
        RestaurantEntity restaurant = database.restaurantDao().findByManagerId(managerId);
        if (restaurant == null) return result;
        for (OrderEntity e : database.orderDao().findByRestaurant(restaurant.id)) {
            String status = e.status == null ? "" : e.status;
            if (OrderStatus.PENDING.equals(status)) {
                result.add(new AppNotification(
                        e.id,
                        "New order received",
                        restaurant.name,
                        e.id,
                        e.pickupTime,
                        e.placedAt,
                        e.status,
                        e.isDelivery
                ));
            } else if (OrderStatus.CANCELLED.equals(status)) {
                result.add(new AppNotification(
                        e.id,
                        "Order cancelled",
                        restaurant.name,
                        e.id,
                        e.pickupTime,
                        e.placedAt,
                        e.status,
                        e.isDelivery
                ));
            }
        }
        return result;
    }

    private static Order toOrder(OrderEntity e) {
        Date placed = new Date(e.placedAt);
        return new Order(e.id, e.restaurantName,
                new SimpleDateFormat("MMM d, yyyy", Locale.US).format(placed),
                new SimpleDateFormat("h:mm a", Locale.US).format(placed),
                e.placedAt, e.itemCount, e.total, e.status, e.restaurantImageUri);
    }

    public void close() {
        database.close();
    }

    private static User toUser(UserEntity user) {
        if (user == null) return null;
        return new User(user.id, user.email, user.role, user.fullName, user.city,
                user.province, user.postalCode, user.emailVerified,
                user.address, user.phone, user.profilePhotoUri);
    }

    // Edit Profile: name, city, province and postal code are required; address and phone are optional.
    public User updateProfile(long userId, String fullName, String address, String city,
                              String province, String postalCode, String phone) {
        User current = getUser(userId);
        if (current == null) throw new IllegalArgumentException("Account no longer exists");
        return updateProfile(userId, fullName, address, city, province, postalCode, phone,
                current.profilePhotoUri);
    }

    public User updateProfile(long userId, String fullName, String address, String city,
                              String province, String postalCode, String phone, String photoUri) {
        int updated = database.userDao().updateProfile(userId, required(fullName),
                address == null ? "" : address.trim(), required(city), required(province),
                required(postalCode), phone == null ? "" : phone.trim(),
                photoUri == null ? "" : photoUri);
        if (updated != 1) throw new IllegalArgumentException("Account no longer exists");
        return getUser(userId);
    }
    private static String required(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Required field");
        return value.trim();
    }

    public static final class DuplicateEmailException extends IllegalArgumentException {
        public DuplicateEmailException() {
            super("Email already registered");
        }
    }

    /** Thrown by placeOrder when an item sold out (or was switched off) before checkout finished. */
    public static final class OutOfStockException extends IllegalStateException {
        public final String foodName;

        public OutOfStockException(String foodName) {
            super("Not enough stock for " + foodName);
            this.foodName = foodName;
        }
    }

    // ---- Customer side: food items from the food_items table -----------------------------

    /** Food a customer can see for this restaurant (switched on by the manager), incl. sold-out items. */
    public List<FoodItem> getFoodForRestaurant(long restaurantId) {
        List<FoodItem> food = new ArrayList<>();
        for (FoodItemEntity e : database.foodItemDao().getAvailableForRestaurant(restaurantId)) {
            food.add(toFoodItem(e));
        }
        return food;
    }

    /** One food item for the Food Detail screen, or null if it is gone or switched off. */
    public FoodItem getFood(long restaurantId, long foodItemId) {
        FoodItemEntity e = database.foodItemDao().getAvailableFoodItem(restaurantId, foodItemId);
        return e == null ? null : toFoodItem(e);
    }

    private static FoodItem toFoodItem(FoodItemEntity e) {
        return new FoodItem((int) e.id, e.name, e.originalPrice, e.discountedPrice,
                e.quantityLeft, emptyToNull(e.availableUntil), 0, e.description,
                emptyToNull(e.imageUri));
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // Generating food item object from FoodItemEntity
    public long createFoodItem(
            long restaurantId,
            String name,
            String description,
            String category,
            double originalPrice,
            double discountedPrice,
            int quantityLeft,
            String availableUntil,
            boolean available,
            String imageUri){

        FoodItemEntity item = new FoodItemEntity();

        item.restaurantId = restaurantId;
        item.name = name;
        item.description = description;
        item.category = category;
        item.originalPrice = originalPrice;
        item.discountedPrice = discountedPrice;
        item.quantityLeft = quantityLeft;
        item.availableUntil = availableUntil;
        item.available = available;
        item.imageUri = imageUri;

        return database.foodItemDao().insert(item);
    }

    // Getting restaurant food items
    public java.util.List<FoodItemEntity> getFoodItemsForRestaurant(long restaurantId){
        return database.foodItemDao().getRestaurantFoodItems(restaurantId);
    }

    public FoodItemEntity getFoodItem(long foodItemId) {
        return database.foodItemDao().getFoodItem(foodItemId);
    }

    //Deleting food items
    public void deleteFoodItem(long foodItemId){
        FoodItemEntity item = database.foodItemDao().getFoodItem(foodItemId);

        if(item != null){
            database.foodItemDao().delete(item);
        }
    }

    //Updating foodItems
    public void updateFoodItem(FoodItemEntity item){
        database.foodItemDao().update(item);
    }

    //Using order entity
    public long createOrder(OrderEntity order){
        return database.orderDao().insert(order);
    }
    public List<OrderEntity> getOrderHistory(long restaurantId){
        return database.orderDao().getOrderHistory(restaurantId);
    }
    public OrderEntity getOrder(long orderId){
        return database.orderDao().getOrder(orderId);
    }
    public void updateOrder(OrderEntity order){
        database.orderDao().update(order);
    }

    public OrderDetails getOrderDetailsForManager(long restaurantId, long orderId) {

        OrderEntity e = database.orderDao().findForRestaurant(restaurantId, orderId);

        if (e == null) {
            return null;
        }

        List<OrderLine> lines = new ArrayList<>();

        for (OrderItemEntity i : database.orderDao().itemsFor(orderId)) {

            lines.add(new OrderLine(i.foodItemId, i.name, i.unitPrice, i.quantity));
        }

        User customer = getUser(e.customerId);

        return new OrderDetails(toOrder(e), lines, e.pickupTime, e.restaurantAddress, e.isDelivery, e.deliveryAddress,
                customer == null ? "" : customer.fullName,
                customer == null ? "" : customer.email,
                customer == null || customer.phone == null ? "" : customer.phone);
    }
}
