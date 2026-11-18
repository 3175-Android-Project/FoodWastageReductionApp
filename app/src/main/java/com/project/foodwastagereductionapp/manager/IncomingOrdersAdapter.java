package com.project.foodwastagereductionapp.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.OrderEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomingOrdersAdapter extends RecyclerView.Adapter<IncomingOrdersAdapter.ViewHolder> {

    private final List<OrderEntity> rows = new ArrayList<>();

    public void setRows(List<OrderEntity> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_incoming_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderEntity o = rows.get(position);
        Context context = holder.itemView.getContext();

        holder.txtOrderNumber.setText(context.getString(R.string.fmtOrderNumber, String.valueOf(o.id)));
        holder.txtStatus.setText(o.status);

        String type = o.isDelivery ? "Delivery" : "Pickup";
        holder.txtCustomerItems.setText("Customer · " + o.itemCount + " items · " + type);

        Date placed = new Date(o.placedAt);
        String timeStr = new SimpleDateFormat("MMM d, h:mm a", Locale.US).format(placed);
        holder.txtTime.setText(timeStr);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtOrderNumber, txtStatus, txtCustomerItems, txtTime;

        ViewHolder(@NonNull View v) {
            super(v);
            txtOrderNumber = v.findViewById(R.id.txtOrderNumber);
            txtStatus = v.findViewById(R.id.txtStatus);
            txtCustomerItems = v.findViewById(R.id.txtCustomerItems);
            txtTime = v.findViewById(R.id.txtTime);
        }
    }
}
