package com.project.foodwastagereductionapp.customer;

import android.content.Context;
import android.content.Intent;

import com.project.foodwastagereductionapp.data.OrderStatus;

/**
 * Customer Orders feature
 *
 * Opens the right screen for an order: the live Order Status screen while the order is in
 * progress, the Order Detail screen once it is completed, rejected or cancelled.
 */
final class OrderNavigator {

    private OrderNavigator() { }

    static void open(Context context, long orderId, String status) {
        Class<?> screen = OrderStatus.isActive(status)
                ? CustomerOrderStatusActivity.class
                : CustomerOrderDetailActivity.class;
        Intent intent = new Intent(context, screen);
        intent.putExtra(CustomerOrderStatusActivity.EXTRA_ORDER_ID, orderId);
        context.startActivity(intent);
    }
}
