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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.FoodItemEntity;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.ui.ConfirmationDialog;

import java.util.ArrayList;
import java.util.List;

public class ManageFoodItemsActivity extends AppCompatActivity {
    private RecyclerView recyclerFoods;
    private FoodAdapter adapter;
    private TextView txtEmpty;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerViewContainer;
    private View linearLayout;
    private static List<FoodItemEntity> cachedManagerFood = null;

    public static void invalidateCache() {
        cachedManagerFood = null;
    }

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

        shimmerViewContainer = findViewById(R.id.shimmerViewContainer);
        linearLayout = findViewById(R.id.linearLayout);

        ((TextView) findViewById(R.id.txtHeaderTitle)).setText(R.string.managerFoodItemsActivity);
        findViewById(R.id.btnBack).setVisibility(View.GONE);

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemActiveIndicatorEnabled(false);
        bottomNav.setSelectedItemId(R.id.nav_items);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_items) {
                return true; // already here
            } else if (id == R.id.nav_home) {
                goTo(ManagerDashboardActivity.class);
                return true;
            } else if (id == R.id.nav_profile) {
                goTo(ManagerProfileActivity.class);
                return true;
            } else if (id == R.id.nav_orders) {
                goTo(IncomingOrdersActivity.class);
                return true;
            }
            return false;
        });

        recyclerFoods = findViewById(R.id.recyclerFoods);
        recyclerFoods.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(
                // Item Click (Edit)
                food -> {
                    Intent intent = new Intent(this, AddFoodItemActivity.class);
                    intent.putExtra("foodId", food.id);
                    startActivity(intent);
                },
                // Delete with Confirmation Dialog
                food -> {
                    ConfirmationDialog.show(
                            this,
                            "Delete food item?",
                            "Are you sure you want to delete this item?",
                            "Delete",
                            "Cancel",
                            () -> {
                                AuthTasks.run(this, null, () -> {
                                    AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
                                    db.deleteFoodItem(food.id);
                                    return true;
                                }, result -> {
                                    invalidateCache();
                                    loadFoodItems();
                                });
                            }
                    );
                },
                // Availability change
                (food, available) -> {
                    AuthTasks.run(this, null, () -> {
                        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
                        food.available = available;
                        db.updateFoodItem(food);
                        return true;
                    }, result -> {
                        invalidateCache();
                    });
                }
        );
        recyclerFoods.setAdapter(adapter);
        txtEmpty = findViewById(R.id.txtEmpty);

        findViewById(R.id.btnAddFood).setOnClickListener(v -> {
            startActivity(new Intent(ManageFoodItemsActivity.this, AddFoodItemActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoodItems();
    }

    private void loadFoodItems() {
        boolean isColdLoad = (cachedManagerFood == null);
        if (isColdLoad) {
            shimmerViewContainer.setVisibility(View.VISIBLE);
            shimmerViewContainer.startShimmer();
            linearLayout.setVisibility(View.GONE);
        } else {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            adapter.setRows(cachedManagerFood);
            txtEmpty.setVisibility(cachedManagerFood.isEmpty() ? View.VISIBLE : View.GONE);
        }

        long startTime = System.currentTimeMillis();
        long managerId = new SessionManager(this).getUserId();
        AuthTasks.run(
                this,
                null,
                () -> {
                    AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
                    Restaurant restaurant = db.getRestaurantForManager(managerId);
                    if (restaurant == null) return new ArrayList<FoodItemEntity>();
                    return db.getFoodItemsForRestaurant(restaurant.id);
                },
                list -> {
                    cachedManagerFood = list;
                    long elapsed = System.currentTimeMillis() - startTime;
                    long delay = isColdLoad ? Math.max(0, 350 - elapsed) : 0;
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        shimmerViewContainer.stopShimmer();
                        shimmerViewContainer.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        adapter.setRows(list);
                        txtEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                    }, delay);
                });
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}
