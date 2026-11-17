package com.project.foodwastagereductionapp.manager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.Restaurant;

public class AddFoodItemActivity extends AppCompatActivity {
    private EditText etFoodName;
    private EditText etDescription;
    private EditText etCategory;
    private EditText etOriginalPrice;
    private EditText etDiscountPrice;
    private EditText etQuantity;
    private EditText etAvailableUntil;
    private Button btnSaveFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_food_item);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFoodName = findViewById(R.id.etFoodName);
        etDescription = findViewById(R.id.etDescription);
        etCategory = findViewById(R.id.etCategory);
        etOriginalPrice = findViewById(R.id.etOriginalPrice);
        etDiscountPrice = findViewById(R.id.etDiscountPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etAvailableUntil = findViewById(R.id.etAvailableUntil);
        btnSaveFood = findViewById(R.id.btnSaveFood);
        btnSaveFood.setOnClickListener(v -> saveFoodItem());

        ((TextView) findViewById(R.id.txtHeaderTitle))
                .setText(R.string.txtAddFoodItemTitle);

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());
    }

    private void saveFoodItem() {
        String foodName = etFoodName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String availableUntil = etAvailableUntil.getText().toString().trim();
        double originalPrice = Double.parseDouble(etOriginalPrice.getText().toString());
        double discountPrice = Double.parseDouble(etDiscountPrice.getText().toString());
        int quantity = Integer.parseInt(etQuantity.getText().toString());
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);

        AuthTasks.run(this, null, () -> {
                    Restaurant restaurant = db.getRestaurantForManager(userId);
                    if (restaurant == null) {
                        return false;
                    }

                    db.createFoodItem(
                            restaurant.id,
                            foodName,
                            description,
                            category,
                            originalPrice,
                            discountPrice,
                            quantity,
                            availableUntil,
                            true,
                            ""
                    );
                    return true;
                },
                result -> {
                    if (Boolean.TRUE.equals(result)) {
                        finish();
                    }
                }
        );
    }
}