package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CustomerProfile;


import java.util.Locale;

public class PriceFormatter {
    public static String format(double price) {
        return String.format(Locale.getDefault(), "$%.2f", price);
    }
}
