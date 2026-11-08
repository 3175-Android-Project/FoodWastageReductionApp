package com.project.foodwastagereductionapp.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.User;
import com.project.foodwastagereductionapp.manager.ManagerDashboardActivity;

public class RestaurantInformationActivity extends AppCompatActivity {
    public static final String EXTRA_EDIT_PROFILE = "editRestaurantProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_information);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        long userId = new SessionManager(this).getUserId();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);

        // Header
        TextView txtHeaderTitle =
                findViewById(R.id.txtHeaderTitle);

        boolean editingProfile = getIntent().getBooleanExtra(EXTRA_EDIT_PROFILE, false);
        txtHeaderTitle.setText(editingProfile ? R.string.txtEditProfileTitle : R.string.app_name);

        ImageView btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Inputs
        EditText editRestaurantName =
                findViewById(R.id.editRestaurantName);

        EditText editRestaurantCity =
                findViewById(R.id.editRestaurantCity);

        EditText editRestaurantProvince =
                findViewById(R.id.editRestaurantProvince);

        EditText editRestaurantPostalCode =
                findViewById(R.id.editRestaurantPostalCode);

        EditText editRestaurantPhone =
                findViewById(R.id.editRestaurantPhone);

        EditText editCuisineType =
                findViewById(R.id.editCuisineType);

        EditText editRestaurantDescription =
                findViewById(R.id.editRestaurantDescription);

        Button btnGoToDashboard = findViewById(R.id.btnGoToDashboard);
        if (editingProfile) btnGoToDashboard.setText(R.string.btnSaveChanges);
        btnGoToDashboard.setEnabled(false);
        AuthTasks.run(this, null, () -> {
            User user = db.getUser(userId);
            Restaurant restaurant = db.getRestaurantForManager(userId);
            return new SetupData(user, restaurant);
        }, data -> {
            if (data.user == null || !User.MANAGER.equals(data.user.role)
                    || !data.user.emailVerified) {
                AuthNavigator.logout(this);
                return;
            }
            // Android restores in-progress text fields itself after a rotation.
            if (savedInstanceState == null) {
                Restaurant restaurant = data.restaurant;
                editRestaurantCity.setText(restaurant == null ? data.user.city : restaurant.city);
                editRestaurantProvince.setText(restaurant == null ? data.user.province : restaurant.province);
                editRestaurantPostalCode.setText(restaurant == null ? data.user.postalCode : restaurant.postalCode);
                if (restaurant != null) {
                    editRestaurantName.setText(restaurant.name);
                    editRestaurantPhone.setText(restaurant.phone);
                    editCuisineType.setText(restaurant.cuisineType);
                    editRestaurantDescription.setText(restaurant.description);
                }
            }
            btnGoToDashboard.setEnabled(true);
        });

        btnGoToDashboard.setOnClickListener(v -> {

            String restaurantName =
                    editRestaurantName.getText().toString().trim();

            String restaurantCity =
                    editRestaurantCity.getText().toString().trim();

            String restaurantProvince =
                    editRestaurantProvince.getText().toString().trim();

            String restaurantPostalCode =
                    editRestaurantPostalCode.getText().toString().trim();

            String phone =
                    editRestaurantPhone.getText().toString().trim();

            String cuisineType =
                    editCuisineType.getText().toString().trim();

            String description =
                    editRestaurantDescription.getText().toString().trim();

            if (restaurantName.isEmpty()
                    || restaurantCity.isEmpty()
                    || restaurantProvince.isEmpty()
                    || restaurantPostalCode.isEmpty()
                    || phone.isEmpty()
                    || cuisineType.isEmpty()) {

                Toast.makeText(
                        RestaurantInformationActivity.this,
                        R.string.msgCompleteAllFields,
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Restaurant restaurant = new Restaurant(0, userId, restaurantName,
                    restaurantCity, restaurantProvince, restaurantPostalCode, phone,
                    cuisineType, description);
            AuthTasks.run(this, btnGoToDashboard, () -> {
                db.saveRestaurant(restaurant);
                return true;
            }, saved -> {
                if (editingProfile) finish();
                else AuthNavigator.open(this, ManagerDashboardActivity.class, userId);
            });
        });
    }

    private static final class SetupData {
        final User user;
        final Restaurant restaurant;
        SetupData(User user, Restaurant restaurant) { this.user = user; this.restaurant = restaurant; }
    }
}
