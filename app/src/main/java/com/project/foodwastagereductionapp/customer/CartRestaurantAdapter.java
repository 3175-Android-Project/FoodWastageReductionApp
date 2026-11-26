package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.ui.LocalImageLoader;

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

import java.util.ArrayList;
import java.util.List;

/** One card per restaurant on the Carts screen. */
public class CartRestaurantAdapter extends RecyclerView.Adapter<CartRestaurantAdapter.ViewHolder> {

    private final List<Restaurant> rows = new ArrayList<>();

    public void setRows(List<Restaurant> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Restaurant r = rows.get(position);
        Context context = h.itemView.getContext();

        // Restaurant photo (same fallback as the other restaurant lists)
        if (r.imageUri != null && !r.imageUri.isEmpty()) {
            LocalImageLoader.load(h.imgRestaurant, r.imageUri);
        } else {
            h.imgRestaurant.setImageResource(R.drawable.icon_store);
        }
        h.txtRestaurantName.setText(r.name);
        h.txtRestaurantAddress.setText(context.getString(R.string.fmtCityProvince, r.city, r.province));

        h.btnViewCart.setOnClickListener(v -> {
            Intent intent = new Intent(context, CheckoutActivity.class);
            intent.putExtra(CheckoutActivity.EXTRA_RESTAURANT_ID, r.id);
            context.startActivity(intent);
        });
        h.btnSeeRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantDetailActivity.class);
            intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, r.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgRestaurant;
        final TextView txtRestaurantName, txtRestaurantAddress;
        final View btnViewCart, btnSeeRestaurant;

        ViewHolder(@NonNull View v) {
            super(v);
            imgRestaurant = v.findViewById(R.id.imgRestaurant);
            txtRestaurantName = v.findViewById(R.id.txtRestaurantName);
            txtRestaurantAddress = v.findViewById(R.id.txtRestaurantAddress);
            btnViewCart = v.findViewById(R.id.btnViewCart);
            btnSeeRestaurant = v.findViewById(R.id.btnSeeRestaurant);
        }
    }
}
