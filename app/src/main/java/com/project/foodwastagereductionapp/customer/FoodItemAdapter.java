package com.project.foodwastagereductionapp.customer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

// Food items on the Restaurant Detail screen, each with an add-to-cart button or quantity stepper.
public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodViewHolder> {

    public interface OnCartChanged {
        void onChanged();
    }

    private final List<FoodItem> rows = new ArrayList<>();
    private final long restaurantId;
    private final OnCartChanged onCartChanged;

    public FoodItemAdapter(long restaurantId, OnCartChanged onCartChanged) {
        this.restaurantId = restaurantId;
        this.onCartChanged = onCartChanged;
    }

    public void setRows(List<FoodItem> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FoodViewHolder(inflater.inflate(R.layout.item_food, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder h, int position) {
        FoodItem f = rows.get(position);
        Context context = h.itemView.getContext();

        if (f.hasImageUri()) {
            h.imgFood.setImageURI(Uri.parse(f.imageUri));
        } else {
            h.imgFood.setImageResource(f.imageRes != 0 ? f.imageRes : R.drawable.icon_food);
        }
        h.txtFoodName.setText(f.name);

        // Discounted price (black)
        h.txtPrice.setText(PriceFormatter.format(f.price));

        // Original price (grey, crossed out) only when there is a real discount
        if (f.originalPrice > f.price) {
            h.txtOriginalPrice.setVisibility(View.VISIBLE);
            h.txtOriginalPrice.setText(PriceFormatter.format(f.originalPrice));
            h.txtOriginalPrice.setPaintFlags(
                    h.txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            h.txtOriginalPrice.setVisibility(View.GONE);
        }

        // Quantity left + pickup time
        if (f.quantityLeft > 0) {
            if (f.availableUntil != null && !f.availableUntil.isEmpty()) {
                h.txtQuantityLeft.setText(context.getString(
                        R.string.fmtQuantityLeftUntil, f.quantityLeft, f.availableUntil));
            } else {
                h.txtQuantityLeft.setText(context.getString(
                        R.string.fmtQuantityLeft, f.quantityLeft));
            }
        } else {
            h.txtQuantityLeft.setText(R.string.msgSoldOut);
        }

        // Stepper vs Single Add button based on cart quantity
        int countInCart = CartManager.quantityOf(restaurantId, f.id);
        if (countInCart > 0) {
            h.btnAdd.setVisibility(View.GONE);
            h.layoutStepper.setVisibility(View.VISIBLE);
            h.txtQuantity.setText(String.valueOf(countInCart));

            // Minus button: decrease quantity
            h.btnMinus.setOnClickListener(v -> {
                CartManager.decrease(restaurantId, f.id);
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos);
                if (onCartChanged != null) onCartChanged.onChanged();
            });

            // Plus button: increase quantity
            h.btnPlus.setOnClickListener(v -> {
                if (CartManager.quantityOf(restaurantId, f.id) >= f.quantityLeft) {
                    Toast.makeText(context, R.string.msgNoMoreStock, Toast.LENGTH_SHORT).show();
                    return;
                }
                CartManager.add(restaurantId, f);
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos);
                if (onCartChanged != null) onCartChanged.onChanged();
            });
        } else {
            h.btnAdd.setVisibility(View.VISIBLE);
            h.layoutStepper.setVisibility(View.GONE);

            if (f.quantityLeft > 0) {
                h.btnAdd.setEnabled(true);
                h.btnAdd.setAlpha(1f);
                h.btnAdd.setOnClickListener(v -> {
                    CartManager.add(restaurantId, f);
                    int pos = h.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos);
                    if (onCartChanged != null) onCartChanged.onChanged();
                });
            } else {
                h.btnAdd.setEnabled(false);
                h.btnAdd.setAlpha(0.4f);
                h.btnAdd.setOnClickListener(null);
            }
        }

        // Tap the row to open Food Detail
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FoodDetailActivity.class);
            intent.putExtra(FoodDetailActivity.EXTRA_RESTAURANT_ID, restaurantId);
            intent.putExtra(FoodDetailActivity.EXTRA_FOOD_ID, f.id);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFood;
        final TextView txtFoodName, txtOriginalPrice, txtPrice, txtQuantityLeft;
        final ImageButton btnAdd;
        final View layoutStepper;
        final TextView btnMinus, txtQuantity, btnPlus;
        final View divider;

        FoodViewHolder(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            txtFoodName = v.findViewById(R.id.txtFoodName);
            txtOriginalPrice = v.findViewById(R.id.txtOriginalPrice);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtQuantityLeft = v.findViewById(R.id.txtQuantityLeft);
            btnAdd = v.findViewById(R.id.btnAdd);
            layoutStepper = v.findViewById(R.id.layoutStepper);
            btnMinus = v.findViewById(R.id.btnMinus);
            txtQuantity = v.findViewById(R.id.txtQuantity);
            btnPlus = v.findViewById(R.id.btnPlus);
            divider = v.findViewById(R.id.divider);
        }
    }
}
