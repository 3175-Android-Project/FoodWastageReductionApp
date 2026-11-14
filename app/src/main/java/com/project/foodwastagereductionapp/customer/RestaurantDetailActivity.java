package com.project.foodwastagereductionapp.customer;

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

        ImageView btnCart = findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> openCart());

        // Food list
        RecyclerView recyclerFood = findViewById(R.id.recyclerFood);
        FoodItemAdapter adapter = new FoodItemAdapter(this::addToCart);
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
    }

    // Database work on a worker thread (AppDatabaseHelper rule), UI updates on the main thread
    private void loadRestaurant(FoodItemAdapter adapter) {
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        new Thread(() -> {
            Restaurant r = db.getRestaurant(restaurantId);
            // Food comes from SampleData until the database has a food items table
            List<FoodItem> food = SampleData.getFoodItems((int) restaurantId);
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

    private void addToCart(FoodItem item) {
        // Don't allow more than what's left
        if (CartManager.quantityOf(item.id) >= item.quantityLeft) {
            Toast.makeText(this, R.string.msgNoMoreStock, Toast.LENGTH_SHORT).show();
            return;
        }
        // One restaurant per cart
        if (!CartManager.add(restaurantId, item)) {
            Toast.makeText(this, R.string.msgCartOtherRestaurant, Toast.LENGTH_SHORT).show();
            return;
        }
        updateCartButton();
    }

    private void updateCartButton() {
        int count = CartManager.count();
        btnViewCart.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        btnViewCart.setText(getString(R.string.fmtViewCart, count));
    }

    private void openCart() {
        // TODO: open the cart/checkout screen once it exists
        Toast.makeText(this, getString(R.string.fmtViewCart, CartManager.count()), Toast.LENGTH_SHORT).show();
    }
}