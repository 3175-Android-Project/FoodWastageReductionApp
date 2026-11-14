package com.project.foodwastagereductionapp.customer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.Restaurant;
import com.project.foodwastagereductionapp.data.RestaurantListing;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the restaurant search results on the customer Browse screen.
 * Each card: name, city/province, and how much surplus food is left.
 */
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final List<RestaurantListing> rows = new ArrayList<>();

    public void setRows(List<RestaurantListing> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new RestaurantViewHolder(inflater.inflate(R.layout.item_restaurant_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        bindRestaurant(holder, rows.get(position));
    }

    private void bindRestaurant(RestaurantViewHolder h, RestaurantListing item) {
        Context context = h.itemView.getContext();
        Restaurant r = item.restaurant;

        h.txtRestaurantName.setText(r.name);
        h.txtRestaurantAddress.setText(r.city + ", " + r.province);
        if (r.imageUri != null && !r.imageUri.isEmpty()) {
            h.imgRestaurant.setImageURI(android.net.Uri.parse(r.imageUri));
        } else {
            h.imgRestaurant.setImageResource(R.drawable.icon_store);
        }

        // Surplus food summary (falls back to cuisine type until food items exist)
        if (item.itemsLeft > 0 && item.availableUntil != null) {
            h.txtAvailability.setText(context.getString(
                    R.string.fmtItemsLeft, item.itemsLeft, item.availableUntil));
        } else {
            h.txtAvailability.setText(r.cuisineType);
        }

        // Tap a card to open that restaurant's food items
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RestaurantDetailActivity.class);
            intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, r.id);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgRestaurant;
        final TextView txtRestaurantName, txtRestaurantAddress, txtAvailability;

        RestaurantViewHolder(@NonNull View v) {
            super(v);
            imgRestaurant = v.findViewById(R.id.imgRestaurant);
            txtRestaurantName = v.findViewById(R.id.txtRestaurantName);
            txtRestaurantAddress = v.findViewById(R.id.txtRestaurantAddress);
            txtAvailability = v.findViewById(R.id.txtAvailability);
        }
    }
}