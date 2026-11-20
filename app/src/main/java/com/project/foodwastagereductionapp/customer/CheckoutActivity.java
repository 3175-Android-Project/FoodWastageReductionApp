package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.OrderLine;
import com.project.foodwastagereductionapp.data.OrderPricing;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends BaseActivity {

    /** The restaurant whose cart is being checked out. */
    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    private long restaurantId;

    private CartLineAdapter adapter;
    private boolean isDelivery = false;

    private TextView txtOrderItemsTitle, txtPickup, txtDelivery;
    private TextView txtTimeLabel, txtTimeValue, txtTimeNote;
    private TextView txtItemsLabel, txtItemsAmount, txtTaxAmount, txtTotalAmount;
    private Button btnPlaceOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        restaurantId = getIntent().getLongExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId == -1) { // opened without a cart
            finish();
            return;
        }
        setContentView(R.layout.activity_checkout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, keyboard));
            return insets;
        });

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleCheckout);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Views
        txtOrderItemsTitle = findViewById(R.id.txtOrderItemsTitle);
        txtPickup = findViewById(R.id.txtPickup);
        txtDelivery = findViewById(R.id.txtDelivery);
        txtTimeLabel = findViewById(R.id.txtTimeLabel);
        txtTimeValue = findViewById(R.id.txtTimeValue);
        txtTimeNote = findViewById(R.id.txtTimeNote);
        txtItemsLabel = findViewById(R.id.txtItemsLabel);
        txtItemsAmount = findViewById(R.id.txtItemsAmount);
        txtTaxAmount = findViewById(R.id.txtTaxAmount);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);

        // Cart items (the +/- buttons call refresh() after changing the cart)
        RecyclerView recyclerCart = findViewById(R.id.recyclerCart);
        adapter = new CartLineAdapter(restaurantId, this::refresh);
        recyclerCart.setAdapter(adapter);

        // Pickup / Delivery toggle
        txtPickup.setOnClickListener(v -> setDelivery(false));
        txtDelivery.setOnClickListener(v -> setDelivery(true));
        setDelivery(false);

        // Place order
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        validateStock();
    }

    /** Re-reads the cart and updates the list, time box and totals. */
    private void refresh() {
        if (CartManager.count(restaurantId) == 0) {
            finish(); // nothing left to check out
            return;
        }
        adapter.setRows(CartManager.getLines(restaurantId));

        int count = CartManager.count(restaurantId);
        txtOrderItemsTitle.setText(getString(R.string.fmtOrderItems, count));

        // Same tax rule as the saved order (OrderPricing), so Checkout and the saved order always match
        double subtotal = CartManager.subtotal(restaurantId);
        double tax = OrderPricing.taxOf(subtotal);
        txtItemsLabel.setText(getString(R.string.fmtItemsCount, count));
        txtItemsAmount.setText(PriceFormatter.format(subtotal));
        txtTaxAmount.setText(PriceFormatter.format(tax));
        txtTotalAmount.setText(PriceFormatter.format(OrderPricing.totalWithTax(subtotal)));

        updateTimeBox();
    }

    /** Verifies that cart items are still available in the database and adjusts if stock changed. */
    private void validateStock() {
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        List<CartManager.CartLine> lines = CartManager.getLines(restaurantId);
        if (lines.isEmpty()) return;

        new Thread(() -> {
            boolean changed = false;
            String warningMsg = null;
            for (CartManager.CartLine line : lines) {
                com.project.foodwastagereductionapp.data.FoodItemEntity entity = db.getFoodItem(line.item.id);
                if (entity == null || !entity.available || entity.quantityLeft <= 0) {
                    while (CartManager.quantityOf(restaurantId, line.item.id) > 0) {
                        CartManager.decrease(restaurantId, line.item.id);
                    }
                    warningMsg = getString(R.string.fmtSoldOutInCart, line.item.name);
                    changed = true;
                } else if (entity.quantityLeft < line.quantity) {
                    while (CartManager.quantityOf(restaurantId, line.item.id) > entity.quantityLeft) {
                        CartManager.decrease(restaurantId, line.item.id);
                    }
                    warningMsg = getString(R.string.fmtSoldOutInCart, line.item.name);
                    changed = true;
                }
            }
            if (changed) {
                final String msg = warningMsg;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (msg != null) {
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                    refresh();
                });
            }
        }).start();
    }

    private void setDelivery(boolean delivery) {
        isDelivery = delivery;
        styleOption(txtPickup, !delivery);
        styleOption(txtDelivery, delivery);
        updateTimeBox();
    }

    private void styleOption(TextView option, boolean selected) {
        if (selected) {
            option.setBackgroundResource(R.drawable.bg_primary_button);
            option.setTextColor(ContextCompat.getColor(this, R.color.text_inverse));
        } else {
            option.setBackground(null);
            option.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    /** Pickup/delivery deadline = the soonest "available until" time in the cart. */
    private void updateTimeBox() {
        String time = CartManager.earliestAvailableUntil(restaurantId);

        txtTimeLabel.setText(isDelivery ? R.string.lblDeliverBy : R.string.lblPickUpBy);
        if (time != null) {
            txtTimeValue.setText(getString(R.string.fmtTodayAt, time));
        } else {
            txtTimeValue.setText(R.string.txtNotSet);
        }

        if (isDelivery) {
            txtTimeNote.setText(R.string.msgDeliveryNote);
        } else if (time != null) {
            txtTimeNote.setText(getString(R.string.fmtPickupBefore, time));
        } else {
            txtTimeNote.setText("");
        }
    }

    // Everything database-related runs on the worker thread; the screen changes on the main thread
    private void placeOrder() {
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);

        // Cart -> order lines (name and price are copied into the order)
        List<OrderLine> lines = new ArrayList<>();
        for (CartManager.CartLine line : CartManager.getLines(restaurantId)) {
            lines.add(new OrderLine(line.item.id, line.item.name, line.item.price, line.quantity));
        }
        String time = CartManager.earliestAvailableUntil(restaurantId);
        String pickupTime = time != null ? getString(R.string.fmtTodayAt, time)
                : getString(R.string.txtNotSet);
        boolean delivery = isDelivery;

        AuthTasks.run(this, btnPlaceOrder, () -> {
            User user = db.getUser(userId);
            if (user == null) throw new IllegalStateException("Account no longer exists");
            // Delivery needs a street address
            if (delivery && !user.hasAddress()) return -1L;
            return db.placeOrder(userId, restaurantId, lines, pickupTime, delivery,
                    delivery ? user.getAddressLine() : null);
        }, orderId -> {
            if (orderId < 0) {
                Toast.makeText(this, R.string.msgAddAddressFirst, Toast.LENGTH_LONG).show();
                return;
            }
            openStatus(orderId);
        }, error -> {
            // An item sold out (or was switched off) while it was in the cart: nothing was saved
            if (error instanceof AppDatabaseHelper.OutOfStockException) {
                String name = ((AppDatabaseHelper.OutOfStockException) error).foodName;
                Toast.makeText(this, getString(R.string.fmtSoldOutInCart, name), Toast.LENGTH_LONG).show();
            } else {
                Log.e("GrabbitDatabase", "Placing the order failed", error);
                Toast.makeText(this, R.string.msgDatabaseError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openStatus(long orderId) {
        CartManager.clear(restaurantId); // only this restaurant's cart, other carts stay
        Toast.makeText(this, R.string.msgOrderPlaced, Toast.LENGTH_SHORT).show();

        // Orders tab underneath, the new order's status on top (Back -> Orders)
        Intent orders = new Intent(this, CustomerOrderActivity.class);
        orders.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent status = new Intent(this, CustomerOrderStatusActivity.class);
        status.putExtra(CustomerOrderStatusActivity.EXTRA_ORDER_ID, orderId);
        startActivities(new Intent[]{orders, status});
        finish();
    }
}
