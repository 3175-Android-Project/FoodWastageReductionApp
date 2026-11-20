package com.project.foodwastagereductionapp.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Carts: one card for every restaurant the customer has food in the cart from.
 * "View Cart" opens Checkout for that restaurant, "See Restaurant" opens its menu.
 */
public class CartsActivity extends BaseActivity {

    private final CartRestaurantAdapter adapter = new CartRestaurantAdapter();
    private View txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleCarts);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView rvCarts = findViewById(R.id.rvCarts);
        rvCarts.setAdapter(adapter);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCarts(); // carts may have changed (an order was placed, items were removed)
    }

    // Restaurant details come from the database (worker thread); the list is drawn on the main thread
    private void loadCarts() {
        List<Long> restaurantIds = CartManager.getRestaurantIds();
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            List<Restaurant> restaurants = new ArrayList<>();
            for (long id : restaurantIds) {
                Restaurant r = db.getRestaurant(id);
                if (r == null) CartManager.clear(id); // restaurant was removed: drop its cart
                else restaurants.add(r);
            }
            return restaurants;
        }, restaurants -> {
            adapter.setRows(restaurants);
            txtEmpty.setVisibility(restaurants.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
