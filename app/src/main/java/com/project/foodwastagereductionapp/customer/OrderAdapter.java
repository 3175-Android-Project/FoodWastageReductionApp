package com.project.foodwastagereductionapp.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list that mixes section titles (String) and orders (Order),
 * e.g. "Current orders", the in-progress orders, "Past orders", the finished orders.
 */
public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ORDER = 1;

    private final List<Object> rows = new ArrayList<>();

    public void setRows(List<Object> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof String ? TYPE_HEADER : TYPE_ORDER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new OrderViewHolder(inflater.inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).txtTitle.setText((String) rows.get(position));
        } else {
            bindOrder((OrderViewHolder) holder, (Order) rows.get(position));
        }
    }

    private void bindOrder(OrderViewHolder h, Order o) {
        Context context = h.itemView.getContext();

        h.imgRestaurant.setImageResource(o.imageRes);
        h.txtRestaurantName.setText(o.restaurantName);
        h.txtOrderNumber.setText(context.getString(R.string.fmtOrderNumber, o.orderNumber));

        String items = context.getResources()
                .getQuantityString(R.plurals.fmtItemCount, o.itemCount, o.itemCount);
        h.txtDateItems.setText(context.getString(R.string.fmtOrderDateTimeItems, o.date, o.time, items));
        h.txtTotal.setText(PriceFormatter.format(o.total));

        // Status text + colour
        h.txtStatus.setText(o.status);
        h.txtStatus.setTextColor(ContextCompat.getColor(context, OrderStatus.textColor(o.status)));

        // Current orders open the live status screen, finished orders open the detail screen
        h.itemView.setOnClickListener(v -> OrderNavigator.open(v.getContext(), o.orderNumber));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView txtTitle;

        HeaderViewHolder(@NonNull View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txtSectionTitle);
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgRestaurant;
        final TextView txtRestaurantName, txtStatus, txtOrderNumber, txtDateItems, txtTotal;

        OrderViewHolder(@NonNull View v) {
            super(v);
            imgRestaurant = v.findViewById(R.id.imgRestaurant);
            txtRestaurantName = v.findViewById(R.id.txtRestaurantName);
            txtStatus = v.findViewById(R.id.txtStatus);
            txtOrderNumber = v.findViewById(R.id.txtOrderNumber);
            txtDateItems = v.findViewById(R.id.txtDateItems);
            txtTotal = v.findViewById(R.id.txtTotal);
        }
    }
}
