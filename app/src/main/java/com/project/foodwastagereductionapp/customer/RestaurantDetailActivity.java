package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.Restaurant;

import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    private long restaurantId;
    private Button btnViewCart;
    private FoodItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Opened without a restaurant -> nothing to show
        restaurantId = getIntent().getLongExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId == -1) {
            finish();
            return;
        }

        // Header
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        View btnCart = findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> openCart());

        // Food list
        RecyclerView recyclerFood = findViewById(R.id.recyclerFood);
        adapter = new FoodItemAdapter(restaurantId, this::updateCartButton);
        recyclerFood.setAdapter(adapter);

        // View Cart button
        btnViewCart = findViewById(R.id.btnViewCart);
        btnViewCart.setOnClickListener(v -> openCart());

        loadRestaurant(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnViewCart != null) updateCartButton(); // cart may have changed on another screen
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (adapter != null) loadRestaurant(adapter); // stock may have changed (e.g. after an order)
    }

    // Database work on a worker thread (AppDatabaseHelper rule), UI updates on the main thread
    private void loadRestaurant(FoodItemAdapter adapter) {
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        new Thread(() -> {
            Restaurant r = db.getRestaurant(restaurantId);
            // Food the manager listed (food_items table)
            List<FoodItem> food = db.getFoodForRestaurant(restaurantId);
            runOnUiThread(() -> {
                if (r == null) {
                    finish();
                    return;
                }
                bindRestaurant(r);
                adapter.setRows(food);
                findViewById(R.id.txtNoFood)
                        .setVisibility(food.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void bindRestaurant(Restaurant r) {
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(r.name);

        TextView txtRestaurantName = findViewById(R.id.txtRestaurantName);
        txtRestaurantName.setText(r.name);

        TextView txtRestaurantAddress = findViewById(R.id.txtRestaurantAddress);
        txtRestaurantAddress.setText(r.city + ", " + r.province);

        TextView txtDescription = findViewById(R.id.txtRestaurantDescription);
        boolean hasDescription = r.description != null && !r.description.trim().isEmpty();
        txtDescription.setText(hasDescription ? r.description : "");
        txtDescription.setVisibility(hasDescription ? View.VISIBLE : View.GONE);

        if (r.imageUri != null && !r.imageUri.isEmpty()) {
            ImageView imgRestaurant = findViewById(R.id.imgRestaurant);
            imgRestaurant.setImageURI(Uri.parse(r.imageUri));
        }
    }

    private void updateCartButton() {
        int count = CartManager.count(restaurantId);
        btnViewCart.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        btnViewCart.setText(getString(R.string.fmtViewCart, count));

        TextView badge = findViewById(R.id.txtCartBadge);
        if (badge != null) {
            badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            badge.setText(count > 99 ? getString(R.string.fmtCartCountMax) : String.valueOf(count));
        }
    }

    private void openCart() {
        if (CartManager.count(restaurantId) == 0) {
            Toast.makeText(this, R.string.msgCartEmpty, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra(CheckoutActivity.EXTRA_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }
}