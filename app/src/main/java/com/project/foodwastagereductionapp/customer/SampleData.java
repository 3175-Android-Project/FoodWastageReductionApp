package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.CustomerProfile;
import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class SampleData {

    public static List<FoodItem> getFoodItems(int restaurantId) {

        List<FoodItem> items = new ArrayList<>();

        if (restaurantId == 1) {

            items.add(
                    new FoodItem(
                            101,
                            "Spicy Tuna Roll",
                            8.99,
                            0,
                            "Fresh tuna with spicy mayo"
                    )
            );

            items.add(
                    new FoodItem(
                            102,
                            "Salmon Nigiri",
                            6.99,
                            0,
                            "Fresh salmon over rice"
                    )
            );

        } else if (restaurantId == 4) {

            items.add(
                    new FoodItem(
                            401,
                            "Cheeseburger",
                            12.99,
                            0,
                            "Classic cheeseburger"
                    )
            );
        }

        return items;
    }


    public static Restaurant getRestaurant(int restaurantId) {

        return new Restaurant(
                restaurantId,
                1,
                "Restaurant " + restaurantId,
                "Vancouver",
                "BC",
                "V6B 1A1",
                "604-555-0100",
                "Mixed",
                "Sample restaurant",
                ""   // imageUri
        );
    }
}