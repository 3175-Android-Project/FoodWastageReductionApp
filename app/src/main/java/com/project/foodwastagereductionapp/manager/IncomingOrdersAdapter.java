package com.project.foodwastagereductionapp.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.OrderEntity;
import com.project.foodwastagereductionapp.data.OrderStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Incoming orders for the restaurant manager. Each card has buttons for the next step:
 * Pending: Reject / Accept, Accepted: Start preparing, Preparing: Mark ready,
 * Ready: Send reminder / Picked up (Delivered for delivery orders).
 */
public class IncomingOrdersAdapter extends RecyclerView.Adapter<IncomingOrdersAdapter.ViewHolder> {

    public interface OnOrderAction {
        /** Move the order to {@code newStatus} (Reject asks for confirmation first). */
        void onChangeStatus(OrderEntity order, String newStatus);

        /** Remind the customer to collect a Ready order. */
        void onSendReminder(OrderEntity order);
    }

    private final List<OrderEntity> rows = new ArrayList<>();
    private final OnOrderAction listener;

    public IncomingOrdersAdapter(OnOrderAction listener) {
        this.listener = listener;
    }

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

        String type = context.getString(o.isDelivery ? R.string.txtMethodDelivery : R.string.txtMethodPickup);
        holder.txtCustomerItems.setText(context.getString(R.string.fmtIncomingOrderInfo, o.itemCount, type));

        Date placed = new Date(o.placedAt);
        String timeStr = new SimpleDateFormat("MMM d, h:mm a", Locale.US).format(placed);
        holder.txtTime.setText(timeStr);

        bindActions(holder, o, context);
    }

    /** Shows the buttons for the order's next step. */
    private void bindActions(ViewHolder h, OrderEntity o, Context context) {
        h.layoutActions.setVisibility(View.VISIBLE);
        h.btnSecondary.setVisibility(View.GONE);
        h.btnPrimary.setVisibility(View.VISIBLE);

        String status = o.status == null ? "" : o.status;
        switch (status) {
            case OrderStatus.PENDING:
                showSecondary(h, context, R.string.btnRejectOrder, true);
                h.btnSecondary.setOnClickListener(v -> listener.onChangeStatus(o, OrderStatus.REJECTED));
                h.btnPrimary.setText(R.string.btnAcceptOrder);
                h.btnPrimary.setOnClickListener(v -> listener.onChangeStatus(o, OrderStatus.ACCEPTED));
                break;
            case OrderStatus.ACCEPTED:
                h.btnPrimary.setText(R.string.btnStartPreparing);
                h.btnPrimary.setOnClickListener(v -> listener.onChangeStatus(o, OrderStatus.PREPARING));
                break;
            case OrderStatus.PREPARING:
                h.btnPrimary.setText(R.string.btnMarkReady);
                h.btnPrimary.setOnClickListener(v -> listener.onChangeStatus(o, OrderStatus.READY));
                break;
            case OrderStatus.READY:
                showSecondary(h, context, R.string.btnSendReminder, false);
                h.btnSecondary.setOnClickListener(v -> listener.onSendReminder(o));
                // Picked up / Delivered both finish the order (saved as Completed)
                h.btnPrimary.setText(o.isDelivery ? R.string.btnMarkDelivered : R.string.btnMarkPickedUp);
                h.btnPrimary.setOnClickListener(v -> listener.onChangeStatus(o, OrderStatus.COMPLETED));
                break;
            default: // Completed, Rejected, Cancelled: nothing left to do
                h.layoutActions.setVisibility(View.GONE);
                break;
        }
    }

    /** Red "Reject" style or light-green "Send reminder" style. */
    private void showSecondary(ViewHolder h, Context context, int textRes, boolean warning) {
        h.btnSecondary.setVisibility(View.VISIBLE);
        h.btnSecondary.setText(textRes);
        h.btnSecondary.setBackgroundResource(warning ? R.drawable.bg_button_warning : R.drawable.bg_icon_square);
        h.btnSecondary.setTextColor(ContextCompat.getColor(context,
                warning ? R.color.text_warning : R.color.background_accent1));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtOrderNumber, txtStatus, txtCustomerItems, txtTime;
        final View layoutActions;
        final Button btnSecondary, btnPrimary;

        ViewHolder(@NonNull View v) {
            super(v);
            txtOrderNumber = v.findViewById(R.id.txtOrderNumber);
            txtStatus = v.findViewById(R.id.txtStatus);
            txtCustomerItems = v.findViewById(R.id.txtCustomerItems);
            txtTime = v.findViewById(R.id.txtTime);
            layoutActions = v.findViewById(R.id.layoutActions);
            btnSecondary = v.findViewById(R.id.btnSecondary);
            btnPrimary = v.findViewById(R.id.btnPrimary);
        }
    }
}