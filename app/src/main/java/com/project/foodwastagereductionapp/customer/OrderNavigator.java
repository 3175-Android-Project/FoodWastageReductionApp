package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CustomerProfile;


import android.content.Context;
import android.content.Intent;

/**
 * Customer Orders feature
 *
 * Opens the right screen for an order: the live Order Status screen while the order is in
 * progress, the Order Detail screen once it is completed, rejected or cancelled.
 */
final class OrderNavigator {

    private OrderNavigator() { }

    static void open(Context context, String orderNumber) {
        OrderStore.PlacedOrder placed = OrderStore.find(orderNumber);
        if (placed == null) return;

        Class<?> screen = OrderStatus.isActive(placed.order.status)
                ? CustomerOrderStatusActivity.class
                : CustomerOrderDetailActivity.class;
        Intent intent = new Intent(context, screen);
        intent.putExtra(CustomerOrderStatusActivity.EXTRA_ORDER_NUMBER, orderNumber);
        context.startActivity(intent);
    }
}
