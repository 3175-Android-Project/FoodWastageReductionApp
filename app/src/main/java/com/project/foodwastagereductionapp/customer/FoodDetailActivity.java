package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.graphics.Paint;
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

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.Restaurant;

public class FoodDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    public static final String EXTRA_FOOD_ID = "food_id";

    private long restaurantId;
    private FoodItem food;
    private Restaurant restaurant;

    private static class FoodDetailData {
        final FoodItem food;
        final Restaurant restaurant;
        FoodDetailData(FoodItem food, Restaurant restaurant) {
            this.food = food;
            this.restaurant = restaurant;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Opened without a food item -> nothing to show
        restaurantId = getIntent().getLongExtra(EXTRA_RESTAURANT_ID, -1);
        int foodId = getIntent().getIntExtra(EXTRA_FOOD_ID, -1);
        if (restaurantId == -1 || foodId == -1) {
            finish();
            return;
        }

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleFoodDetails);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Add to cart (enabled once the food has loaded)
        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        btnAddToCart.setEnabled(false);
        btnAddToCart.setOnClickListener(v -> addToCart());

        loadFood(foodId);
    }

    // Food the manager listed (food_items table), read on the database worker thread
    private void loadFood(int foodId) {
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            FoodItem f = db.getFood(restaurantId, foodId);
            Restaurant r = db.getRestaurant(restaurantId);
            return new FoodDetailData(f, r);
        }, result -> {
            if (result == null || result.food == null) { // removed or switched off by the restaurant
                Toast.makeText(this, R.string.msgFoodNoLongerAvailable, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            food = result.food;
            restaurant = result.restaurant;
            bindFood();
        });
    }

    private void bindFood() {
        ImageView imgFood = findViewById(R.id.imgFood);
        if (food.hasImageUri()) {
            imgFood.setImageURI(Uri.parse(food.imageUri));
        } else {
            imgFood.setImageResource(food.imageRes != 0 ? food.imageRes : R.drawable.icon_food);
        }

        if (restaurant != null) {
            TextView txtRestaurantName = findViewById(R.id.txtRestaurantName);
            if (txtRestaurantName != null) {
                txtRestaurantName.setText(restaurant.name);
            }
            View layoutRestaurant = findViewById(R.id.layoutRestaurant);
            if (layoutRestaurant != null) {
                layoutRestaurant.setOnClickListener(v -> openRestaurant());
            }
        }

        TextView txtFoodName = findViewById(R.id.txtFoodName);
        txtFoodName.setText(food.name);

        // Discounted price (green)
        TextView txtPrice = findViewById(R.id.txtPrice);
        txtPrice.setText(PriceFormatter.format(food.price));

        // Original price (grey, crossed out) only when there is a real discount
        TextView txtOriginalPrice = findViewById(R.id.txtOriginalPrice);
        if (food.originalPrice > food.price) {
            txtOriginalPrice.setVisibility(View.VISIBLE);
            txtOriginalPrice.setText(PriceFormatter.format(food.originalPrice));
            txtOriginalPrice.setPaintFlags(
                    txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            txtOriginalPrice.setVisibility(View.GONE);
        }

        TextView txtDescription = findViewById(R.id.txtFoodDescription);
        txtDescription.setText(food.description);

        // Info box
        TextView txtAvailableUntil = findViewById(R.id.txtAvailableUntil);
        if (food.availableUntil != null && !food.availableUntil.isEmpty()) {
            txtAvailableUntil.setText(getString(R.string.fmtTodayAt, food.availableUntil));
        } else {
            txtAvailableUntil.setText(R.string.txtNotSet);
        }

        TextView txtAvailability = findViewById(R.id.txtAvailability);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        if (food.quantityLeft > 0) {
            txtAvailability.setText(getString(R.string.fmtQuantityLeft, food.quantityLeft));
            btnAddToCart.setEnabled(true);
            btnAddToCart.setAlpha(1f);
        } else {
            txtAvailability.setText(R.string.msgSoldOut);
            btnAddToCart.setEnabled(false);
            btnAddToCart.setAlpha(0.4f);
        }
    }

    private void openRestaurant() {
        Intent intent = new Intent(this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }

    private void addToCart() {
        if (food == null) return; // still loading
        // Don't allow more than what's left
        if (CartManager.quantityOf(restaurantId, food.id) >= food.quantityLeft) {
            Toast.makeText(this, R.string.msgNoMoreStock, Toast.LENGTH_SHORT).show();
            return;
        }
        // Each restaurant has its own cart
        CartManager.add(restaurantId, food);
        Toast.makeText(this, R.string.msgAddedToCart, Toast.LENGTH_SHORT).show();
        // back to the restaurant; its View Cart button updates in onResume()
        finish();
    }
}