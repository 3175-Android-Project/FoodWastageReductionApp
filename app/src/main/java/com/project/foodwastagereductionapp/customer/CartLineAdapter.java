package com.project.foodwastagereductionapp.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.FoodItem;

import java.util.ArrayList;
import java.util.List;

/** Cart rows on the Checkout screen, each with a - / qty / + stepper. */
public class CartLineAdapter extends RecyclerView.Adapter<CartLineAdapter.CartLineViewHolder> {

    public interface OnCartChanged {
        void onChanged();
    }

    private final List<CartManager.CartLine> rows = new ArrayList<>();
    private final long restaurantId;
    private final OnCartChanged onChanged;

    public CartLineAdapter(long restaurantId, OnCartChanged onChanged) {
        this.restaurantId = restaurantId;
        this.onChanged = onChanged;
    }

    public void setRows(List<CartManager.CartLine> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new CartLineViewHolder(inflater.inflate(R.layout.item_cart_line, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartLineViewHolder h, int position) {
        CartManager.CartLine line = rows.get(position);
        FoodItem f = line.item;

        if (f.hasImageUri()) {
            h.imgFood.setImageURI(Uri.parse(f.imageUri));
        } else {
            h.imgFood.setImageResource(f.imageRes != 0 ? f.imageRes : R.drawable.icon_food);
        }
        h.txtFoodName.setText(f.name);
        h.txtPrice.setText(PriceFormatter.format(f.price));
        h.txtQuantity.setText(String.valueOf(line.quantity));

        // - : remove one (the row disappears at 0)
        h.btnMinus.setOnClickListener(v -> {
            CartManager.decrease(restaurantId, f.id);
            onChanged.onChanged();
        });

        // + : add one, but not more than what's left
        h.btnPlus.setOnClickListener(v -> {
            if (CartManager.quantityOf(restaurantId, f.id) >= f.quantityLeft) {
                Toast.makeText(v.getContext(), R.string.msgNoMoreStock, Toast.LENGTH_SHORT).show();
                return;
            }
            CartManager.add(restaurantId, f);
            onChanged.onChanged();
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class CartLineViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFood;
        final TextView txtFoodName, txtPrice, txtQuantity, btnMinus, btnPlus;

        CartLineViewHolder(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            txtFoodName = v.findViewById(R.id.txtFoodName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtQuantity = v.findViewById(R.id.txtQuantity);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
        }
    }
}