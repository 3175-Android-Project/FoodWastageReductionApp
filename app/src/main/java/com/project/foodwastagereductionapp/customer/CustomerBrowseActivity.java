package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.RestaurantListing;

import java.util.List;

public class CustomerBrowseActivity extends AppCompatActivity {

    private RestaurantAdapter adapter;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_browse);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleRestaurants);

        findViewById(R.id.btnBack).setVisibility(View.GONE);

        // System back (gesture/button) -> Home, not exit
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goTo(CustomerHomeActivity.class);
            }
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_search);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_search) {
                return true; // already here
            } else if (id == R.id.nav_home) {
                goTo(CustomerHomeActivity.class);
                return true;
            } else if (id == R.id.nav_orders) {
                goTo(CustomerOrderActivity.class);
                return true;
            } else if (id == R.id.nav_profile) {
                goTo(CustomerProfileActivity.class);
                return true;
            }
            return false;
        });

        // Search results list (card tap is handled inside RestaurantAdapter)
        txtEmpty = findViewById(R.id.txtEmpty);
        RecyclerView recycler = findViewById(R.id.recyclerRestaurants);
        adapter = new RestaurantAdapter();
        recycler.setAdapter(adapter);

        // Search box: filter as the user types
        EditText editSearch = findViewById(R.id.editSearch);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                loadRestaurants(s.toString());
            }
        });

        loadRestaurants(""); // show all restaurants on open
    }

    private void loadRestaurants(String query) {
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        new Thread(() -> {
            List<RestaurantListing> results = db.searchRestaurants(query);
            runOnUiThread(() -> {
                adapter.setRows(results);
                txtEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void goTo(Class<?> screen) {
        startActivity(new Intent(this, screen));
        overridePendingTransition(0, 0);
        finish();
    }
}