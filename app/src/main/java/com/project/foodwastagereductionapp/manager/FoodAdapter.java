package com.project.foodwastagereductionapp.manager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.FoodItemEntity;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter
        extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnDeleteClick {
        void onDelete(FoodItemEntity item);
    }

    public interface OnAvailabilityChange {
        void onAvailabilityChanged(
                FoodItemEntity item,
                boolean available
        );
    }

    private final List<FoodItemEntity> rows =
            new ArrayList<>();

    private final OnDeleteClick onDeleteClick;
    private final OnAvailabilityChange onAvailabilityChange;

    public FoodAdapter(
            OnDeleteClick onDeleteClick,
            OnAvailabilityChange onAvailabilityChange) {

        this.onDeleteClick = onDeleteClick;
        this.onAvailabilityChange = onAvailabilityChange;
    }

    public void setRows(
            List<FoodItemEntity> newRows) {

        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        LayoutInflater inflater =
                LayoutInflater.from(parent.getContext());

        return new FoodViewHolder(
                inflater.inflate(
                        R.layout.item_food_manager,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull FoodViewHolder holder,
            int position) {

        FoodItemEntity food =
                rows.get(position);

        holder.txtFoodName.setText(
                food.name
        );

        holder.txtFoodPrice.setText(
                "$" +
                        food.discountedPrice +
                        " • " +
                        food.quantityLeft +
                        " left"
        );

        if (food.available) {

            holder.txtQuantity.setText(
                    "Available"
            );

            holder.txtQuantity.setTextColor(
                    Color.parseColor("#4CAF50")
            );

        } else {

            holder.txtQuantity.setText(
                    "Out of stock"
            );

            holder.txtQuantity.setTextColor(
                    Color.parseColor("#F44336")
            );
        }

        holder.switchAvailable.setChecked(
                food.available
        );

        holder.btnDelete.setOnClickListener(
                v -> onDeleteClick.onDelete(food)
        );

        holder.switchAvailable
                .setOnCheckedChangeListener(
                        (buttonView, isChecked) ->
                                onAvailabilityChange
                                        .onAvailabilityChanged(
                                                food,
                                                isChecked
                                        )
                );
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class FoodViewHolder
            extends RecyclerView.ViewHolder {

        final TextView txtFoodName;
        final TextView txtFoodPrice;
        final TextView txtQuantity;

        final Switch switchAvailable;

        final Button btnDelete;

        FoodViewHolder(
                @NonNull View v) {

            super(v);

            txtFoodName =
                    v.findViewById(R.id.txtFoodName);

            txtFoodPrice =
                    v.findViewById(R.id.txtFoodPrice);

            txtQuantity =
                    v.findViewById(R.id.txtQuantity);

            switchAvailable =
                    v.findViewById(R.id.switchAvailable);

            btnDelete =
                    v.findViewById(R.id.btnDelete);
        }
    }
}
