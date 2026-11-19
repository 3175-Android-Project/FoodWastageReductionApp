package com.project.foodwastagereductionapp.manager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.FoodItemEntity;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnItemClick {
        void onClick(FoodItemEntity item);
    }

    public interface OnDeleteClick {
        void onDelete(FoodItemEntity item);
    }

    public interface OnAvailabilityChange {
        void onAvailabilityChanged(FoodItemEntity item, boolean available);
    }

    private final List<FoodItemEntity> rows = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnDeleteClick onDeleteClick;
    private final OnAvailabilityChange onAvailabilityChange;

    public FoodAdapter(OnItemClick onItemClick, OnDeleteClick onDeleteClick, OnAvailabilityChange onAvailabilityChange) {
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
        this.onAvailabilityChange = onAvailabilityChange;
    }

    public void setRows(List<FoodItemEntity> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FoodViewHolder(inflater.inflate(R.layout.item_food_manager, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItemEntity food = rows.get(position);

        holder.txtFoodName.setText(food.name);
        holder.txtFoodPrice.setText("$" + food.discountedPrice + " · " + food.quantityLeft + " left");

        if (food.quantityLeft <= 0) {
            holder.txtStatus.setText("Out of stock");
            holder.txtStatus.setTextColor(Color.parseColor("#C62828"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
            holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            holder.txtStatus.setPadding(24, 6, 24, 6);
            if (food.available) {
                food.available = false;
                onAvailabilityChange.onAvailabilityChanged(food, false);
            }
        } else if (!food.available) {
            holder.txtStatus.setText("Unavailable");
            holder.txtStatus.setTextColor(Color.parseColor("#64748B"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
            holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            holder.txtStatus.setPadding(24, 6, 24, 6);
        } else {
            holder.txtStatus.setText("Available");
            holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
            holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            holder.txtStatus.setPadding(24, 6, 24, 6);
        }

        if (food.imageUri != null && !food.imageUri.isEmpty()) {
            holder.imgFood.setImageURI(Uri.parse(food.imageUri));
            holder.imgFood.setImageTintList(null);
        } else {
            holder.imgFood.setImageResource(R.drawable.icon_food);
            holder.imgFood.setImageTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
        }

        holder.switchAvailable.setChecked(food.available);
        holder.itemView.setOnClickListener(v -> onItemClick.onClick(food));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.onDelete(food));
        holder.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                food.available = isChecked;
                if (food.quantityLeft <= 0) {
                    holder.txtStatus.setText("Out of stock");
                    holder.txtStatus.setTextColor(Color.parseColor("#C62828"));
                    holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
                    holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
                    holder.txtStatus.setPadding(24, 6, 24, 6);
                } else if (!isChecked) {
                    holder.txtStatus.setText("Unavailable");
                    holder.txtStatus.setTextColor(Color.parseColor("#64748B"));
                    holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
                    holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                    holder.txtStatus.setPadding(24, 6, 24, 6);
                } else {
                    holder.txtStatus.setText("Available");
                    holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
                    holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
                    holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                    holder.txtStatus.setPadding(24, 6, 24, 6);
                }
                onAvailabilityChange.onAvailabilityChanged(food, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView imgFood;
        final TextView txtFoodName;
        final TextView txtFoodPrice;
        final TextView txtStatus;
        final SwitchCompat switchAvailable;
        final ImageView btnDelete;

        FoodViewHolder(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            txtFoodName = v.findViewById(R.id.txtFoodName);
            txtFoodPrice = v.findViewById(R.id.txtFoodPrice);
            txtStatus = v.findViewById(R.id.txtStatus);
            switchAvailable = v.findViewById(R.id.switchAvailable);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
