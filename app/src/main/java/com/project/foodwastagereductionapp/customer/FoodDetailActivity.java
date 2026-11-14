package com.project.foodwastagereductionapp.customer;

import android.graphics.Paint;
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
import com.project.foodwastagereductionapp.data.FoodItem;

public class FoodDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    public static final String EXTRA_FOOD_ID = "food_id";

    private long restaurantId;
    private FoodItem food;

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
        food = findFood(restaurantId, foodId);
        if (food == null) {
            finish();
            return;
        }

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleFoodDetails);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        bindFood();

        // Add to cart
        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    // Food comes from SampleData until the database has a food items table
    private static FoodItem findFood(long restaurantId, int foodId) {
        if (restaurantId == -1 || foodId == -1) return null;
        for (FoodItem f : SampleData.getFoodItems((int) restaurantId)) {
            if (f.id == foodId) return f;
        }
        return null;
    }

    private void bindFood() {
        ImageView imgFood = findViewById(R.id.imgFood);
        imgFood.setImageResource(food.imageRes != 0 ? food.imageRes : R.drawable.icon_food);

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

    private void addToCart() {
        // Don't allow more than what's left
        if (CartManager.quantityOf(food.id) >= food.quantityLeft) {
            Toast.makeText(this, R.string.msgNoMoreStock, Toast.LENGTH_SHORT).show();
            return;
        }
        // One restaurant per cart
        if (!CartManager.add(restaurantId, food)) {
            Toast.makeText(this, R.string.msgCartOtherRestaurant, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.msgAddedToCart, Toast.LENGTH_SHORT).show();
        // back to the restaurant; its View Cart button updates in onResume()
        finish();
    }
}