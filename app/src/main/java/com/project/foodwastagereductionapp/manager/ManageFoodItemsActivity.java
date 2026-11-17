package com.project.foodwastagereductionapp.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.customer.FoodItemAdapter;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.data.Restaurant;

public class ManageFoodItemsActivity extends AppCompatActivity {
    private RecyclerView recyclerFoods;
    private FoodAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_food_items);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.managerFoodItemsActivity);
        findViewById(R.id.btnBack).setVisibility(View.GONE);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemActiveIndicatorEnabled(false);
        bottomNav.setSelectedItemId(R.id.nav_items);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_items) return true;
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ManagerDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ManagerProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        recyclerFoods = findViewById(R.id.recyclerFoods);
        recyclerFoods.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(
                //Delete
                food -> {

                    AuthTasks.run(
                            this,
                            null,
                            () -> {
                                AppDatabaseHelper db =
                                        AppDatabaseHelper.getInstance(this);
                                db.deleteFoodItem(food.id);
                                return true;
                            },
                            result -> {
                                recreate();
                            }
                    );
                },
                //Avaiablility
                (food, available) -> {

                    AuthTasks.run(this, null, () -> {
                                AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
                                food.available = available;
                                db.updateFoodItem(food);
                                return true;
                            },
                            result -> {

                            }
                    );
                }
        );
        recyclerFoods.setAdapter(adapter);

        long managerId = new SessionManager(this).getUserId();
        AuthTasks.run(
                this,
                null,
                () -> {
                    AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
                    Restaurant restaurant = db.getRestaurantForManager(managerId);
                    return db.getFoodItemsForRestaurant(restaurant.id);
                },
                list -> {
                    adapter.setRows(list);
                });
    }
}