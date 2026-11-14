package com.project.foodwastagereductionapp.customer;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.FoodItem;

import java.util.ArrayList;
import java.util.List;


//Food items on the Restaurant Detail screen, each with an add-to-cart button. */
public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodViewHolder> {

    public interface OnAddClick {
        void onAdd(FoodItem item);
    }

    private final List<FoodItem> rows = new ArrayList<>();
    private final OnAddClick onAdd;

    public FoodItemAdapter(OnAddClick onAdd) {
        this.onAdd = onAdd;
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

        h.imgFood.setImageResource(f.imageRes != 0 ? f.imageRes : R.drawable.icon_food);
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

        // Quantity left
        if (f.quantityLeft > 0) {
            h.txtQuantityLeft.setText(context.getString(R.string.fmtQuantityLeft, f.quantityLeft));
            h.btnAdd.setEnabled(true);
            h.btnAdd.setAlpha(1f);
        } else {
            h.txtQuantityLeft.setText(R.string.msgSoldOut);
            h.btnAdd.setEnabled(false);
            h.btnAdd.setAlpha(0.4f);
        }

        h.btnAdd.setOnClickListener(v -> onAdd.onAdd(f));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFood;
        final TextView txtFoodName, txtOriginalPrice, txtPrice, txtQuantityLeft;
        final ImageButton btnAdd;

        FoodViewHolder(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            txtFoodName = v.findViewById(R.id.txtFoodName);
            txtOriginalPrice = v.findViewById(R.id.txtOriginalPrice);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtQuantityLeft = v.findViewById(R.id.txtQuantityLeft);
            btnAdd = v.findViewById(R.id.btnAdd);
        }
    }
}
