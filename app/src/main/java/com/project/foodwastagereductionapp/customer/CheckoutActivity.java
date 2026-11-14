package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.CustomerProfile;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.User;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_checkout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        adapter = new CartLineAdapter(this::refresh);
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
    }

    /** Re-reads the cart and updates the list, time box and totals. */
    private void refresh() {
        if (CartManager.count() == 0) {
            finish(); // nothing left to check out
            return;
        }
        adapter.setRows(CartManager.getLines());

        int count = CartManager.count();
        txtOrderItemsTitle.setText(getString(R.string.fmtOrderItems, count));

        // Same tax rule as OrderStore, so Checkout and the saved order always match
        double subtotal = CartManager.subtotal();
        double tax = OrderStore.taxOf(subtotal);
        txtItemsLabel.setText(getString(R.string.fmtItemsCount, count));
        txtItemsAmount.setText(PriceFormatter.format(subtotal));
        txtTaxAmount.setText(PriceFormatter.format(tax));
        txtTotalAmount.setText(PriceFormatter.format(subtotal + tax));

        updateTimeBox();
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
        String time = CartManager.earliestAvailableUntil();

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

    // Restaurant + customer come from the database (worker thread); the order is saved on the main thread
    private void placeOrder() {
        btnPlaceOrder.setEnabled(false); // avoid double orders
        long restaurantId = CartManager.getRestaurantId();
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);

        new Thread(() -> {
            Restaurant restaurant = db.getRestaurant(restaurantId);
            User user = db.getUser(userId);
            runOnUiThread(() -> {
                if (restaurant == null || user == null) {
                    Toast.makeText(this, R.string.msgDatabaseError, Toast.LENGTH_SHORT).show();
                    btnPlaceOrder.setEnabled(true);
                    return;
                }
                // Delivery needs a street address
                if (isDelivery && !user.hasAddress()) {
                    Toast.makeText(this, R.string.msgAddAddressFirst, Toast.LENGTH_LONG).show();
                    btnPlaceOrder.setEnabled(true);
                    return;
                }
                saveOrderAndOpenStatus(restaurant, user);
            });
        }).start();
    }

    private void saveOrderAndOpenStatus(Restaurant restaurant, User user) {
        // Cart -> the team's CartItem list
        List<CartItem> items = new ArrayList<>();
        for (CartManager.CartLine line : CartManager.getLines()) {
            items.add(new CartItem(line.item, line.quantity));
        }

        String time = CartManager.earliestAvailableUntil();
        String pickupTime = time != null ? getString(R.string.fmtTodayAt, time)
                : getString(R.string.txtNotSet);
        String deliveryAddress = isDelivery ? user.getAddressLine() : null;

        OrderStore.PlacedOrder placed = OrderStore.place(
                restaurant, items, pickupTime, isDelivery, deliveryAddress);
        CartManager.clear();
        Toast.makeText(this, R.string.msgOrderPlaced, Toast.LENGTH_SHORT).show();

        // Orders tab underneath, the new order's status on top (Back -> Orders)
        Intent orders = new Intent(this, CustomerOrderActivity.class);
        orders.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent status = new Intent(this, CustomerOrderStatusActivity.class);
        status.putExtra(CustomerOrderStatusActivity.EXTRA_ORDER_NUMBER, placed.order.orderNumber);
        startActivities(new Intent[]{orders, status});
        finish();
    }
}