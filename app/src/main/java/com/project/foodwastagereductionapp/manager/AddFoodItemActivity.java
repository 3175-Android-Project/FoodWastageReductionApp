package com.project.foodwastagereductionapp.manager;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.FoodItemEntity;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.ui.BaseActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddFoodItemActivity extends BaseActivity {
    private EditText etFoodName;
    private EditText etDescription;
    private EditText etCategory;
    private EditText etOriginalPrice;
    private EditText etDiscountPrice;
    private EditText etQuantity;
    private EditText etAvailableUntil;
    private Button btnSaveFood;
    private ShapeableImageView imgFood;
    private TextView txtAddImage;
    private Uri selectedImageUri;
    private long foodId = -1;

    private final ActivityResultLauncher<String[]> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            try {
                                getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );
                            } catch (SecurityException ignored) {
                            }
                            selectedImageUri = uri;
                            imgFood.setImageURI(uri);
                            txtAddImage.setVisibility(View.GONE);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_food_item);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
            return insets;
        });

        foodId = getIntent().getLongExtra("foodId", -1);

        etFoodName = findViewById(R.id.etFoodName);
        etDescription = findViewById(R.id.etDescription);
        etCategory = findViewById(R.id.etCategory);
        etOriginalPrice = findViewById(R.id.etOriginalPrice);
        etDiscountPrice = findViewById(R.id.etDiscountPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etAvailableUntil = findViewById(R.id.etAvailableUntil);
        btnSaveFood = findViewById(R.id.btnSaveFood);
        imgFood = findViewById(R.id.imgFood);
        txtAddImage = findViewById(R.id.txtAddImage);

        etAvailableUntil.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, hourOfDay, min) -> {
                String amPm = hourOfDay >= 12 ? "PM" : "AM";
                int h = hourOfDay == 0 ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay);
                String timeStr = String.format(Locale.US, "%d:%02d %s", h, min, amPm);
                etAvailableUntil.setText(timeStr);
            }, hour, minute, false).show();
        });

        findViewById(R.id.layoutFoodImage).setOnClickListener(v ->
                imagePicker.launch(new String[]{"image/*"})
        );

        btnSaveFood.setOnClickListener(v -> saveFoodItem());

        if (foodId != -1) {
            ((TextView) findViewById(R.id.txtHeaderTitle)).setText("Edit Food Item");
            ((TextView) findViewById(R.id.txtFoodInformationTitle)).setText("Edit Food Item");
            btnSaveFood.setText("Save Changes");

            AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
            AuthTasks.run(this, null, () -> db.getFoodItem(foodId), item -> {
                if (item != null) {
                    etFoodName.setText(item.name);
                    etCategory.setText(item.category);
                    etOriginalPrice.setText(String.valueOf(item.originalPrice));
                    etDiscountPrice.setText(String.valueOf(item.discountedPrice));
                    etQuantity.setText(String.valueOf(item.quantityLeft));
                    etAvailableUntil.setText(item.availableUntil);
                    etDescription.setText(item.description);
                    if (item.imageUri != null && !item.imageUri.isEmpty()) {
                        selectedImageUri = Uri.parse(item.imageUri);
                        imgFood.setImageURI(selectedImageUri);
                        txtAddImage.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.txtAddFoodItemTitle);
        }

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());
    }

    private void saveFoodItem() {
        String foodName = etFoodName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String availableUntil = etAvailableUntil.getText().toString().trim();
        String origPriceStr = etOriginalPrice.getText().toString().trim();
        String discPriceStr = etDiscountPrice.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();

        if (foodName.isEmpty()) {
            etFoodName.setError("Food name is required");
            etFoodName.requestFocus();
            return;
        }
        if (category.isEmpty()) {
            etCategory.setError("Category is required");
            etCategory.requestFocus();
            return;
        }
        if (origPriceStr.isEmpty()) {
            etOriginalPrice.setError("Original price is required");
            etOriginalPrice.requestFocus();
            return;
        }
        if (discPriceStr.isEmpty()) {
            etDiscountPrice.setError("Discount price is required");
            etDiscountPrice.requestFocus();
            return;
        }
        if (qtyStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }
        if (availableUntil.isEmpty()) {
            etAvailableUntil.setError("Available until time is required");
            etAvailableUntil.requestFocus();
            return;
        }

        double originalPrice, discountPrice;
        int quantity;
        try {
            originalPrice = Double.parseDouble(origPriceStr);
            discountPrice = Double.parseDouble(discPriceStr);
            quantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for price and quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (originalPrice <= 0) {
            Toast.makeText(this, "Original price must be greater than 0.", Toast.LENGTH_LONG).show();
            etOriginalPrice.requestFocus();

            return;
        }

        if (discountPrice >= originalPrice) {
            etDiscountPrice.setError("Discounted price must be lower than the original price");
            etDiscountPrice.requestFocus();

            Toast.makeText(this, "Discounted price must be lower than the original price.", Toast.LENGTH_LONG).show();

            return;
        }

        if (quantity <= 0) {
            Toast.makeText(this, "Quantity must be greater than 0.", Toast.LENGTH_LONG).show();
            etQuantity.requestFocus();

            return;
        }


        String imageUri = selectedImageUri == null ? "" : selectedImageUri.toString();
        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);

        AuthTasks.run(this, null, () -> {
                    if (foodId != -1) {
                        FoodItemEntity item = db.getFoodItem(foodId);
                        if (item != null) {
                            item.name = foodName;
                            item.description = description;
                            item.category = category;
                            item.originalPrice = originalPrice;
                            item.discountedPrice = discountPrice;
                            item.quantityLeft = quantity;
                            item.availableUntil = availableUntil;
                            item.imageUri = imageUri;
                            db.updateFoodItem(item);
                        }
                    } else {
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
                                imageUri
                        );
                    }
                    return true;
                },
                result -> {
                    if (Boolean.TRUE.equals(result)) {
                        ManageFoodItemsActivity.invalidateCache();
                        finish();
                    }
                }
        );
    }
}
