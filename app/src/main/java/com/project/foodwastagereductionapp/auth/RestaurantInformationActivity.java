package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
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
import com.project.foodwastagereductionapp.manager.ManagerDashboardActivity;

public class RestaurantInformationActivity extends AppCompatActivity {

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

        String city =
                getIntent().getStringExtra("city");

        String province =
                getIntent().getStringExtra("province");

        String postalCode =
                getIntent().getStringExtra("postalCode");

        // Header
        TextView txtHeaderTitle =
                findViewById(R.id.txtHeaderTitle);

        txtHeaderTitle.setText(R.string.app_name);

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

        // Use Account Information as default values
        if (city != null) {
            editRestaurantCity.setText(city);
        }

        if (province != null) {
            editRestaurantProvince.setText(province);
        }

        if (postalCode != null) {
            editRestaurantPostalCode.setText(postalCode);
        }

        Button btnGoToDashboard =
                findViewById(R.id.btnGoToDashboard);

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

            // SQLite restaurant saving will be added later

            Intent intent = new Intent(
                    RestaurantInformationActivity.this,
                    ManagerDashboardActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}