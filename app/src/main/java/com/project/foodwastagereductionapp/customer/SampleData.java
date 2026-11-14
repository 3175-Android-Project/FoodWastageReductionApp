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

        if (restaurantId == 1) {            // Happy Sushi
            items.add(new FoodItem(101, "Spicy Tuna Roll", 12.00, 8.99, 5, "5:00 PM", 0,
                    "Fresh tuna with spicy mayo"));
            items.add(new FoodItem(102, "Salmon Nigiri", 9.50, 6.99, 3, "5:00 PM", 0,
                    "Fresh salmon over rice"));

        } else if (restaurantId == 2) {     // Kandy Thai Kitchen
            items.add(new FoodItem(201, "Pad Thai", 14.00, 9.99, 4, "8:30 PM", 0,
                    "Rice noodles with tamarind sauce"));
            items.add(new FoodItem(202, "Green Curry", 15.00, 10.50, 2, "8:30 PM", 0,
                    "Chicken green curry with jasmine rice"));

        } else if (restaurantId == 3) {     // Burnaby Bakery
            items.add(new FoodItem(301, "Croissant Box (4)", 12.00, 6.00, 6, "6:00 PM", 0,
                    "Butter croissants baked this morning"));
            items.add(new FoodItem(302, "Sourdough Loaf", 8.00, 4.50, 3, "6:00 PM", 0,
                    "Fresh sourdough bread"));

        } else if (restaurantId == 4) {     // Green Leaf Cafe
            items.add(new FoodItem(401, "Caesar Salad", 11.00, 7.50, 4, "4:00 PM", 0,
                    "Romaine, parmesan and croutons"));
            items.add(new FoodItem(402, "Turkey Sandwich", 10.50, 6.99, 0, "4:00 PM", 0,
                    "Sold out example"));

        } else if (restaurantId == 5) {     // Pho Metro
            items.add(new FoodItem(501, "Beef Pho", 16.00, 11.00, 5, "9:00 PM", 0,
                    "Rice noodle soup with beef"));
            items.add(new FoodItem(502, "Banh Mi", 9.00, 5.99, 3, "9:00 PM", 0,
                    "Vietnamese baguette sandwich"));

        } else if (restaurantId == 6) {     // Pizza Corner
            items.add(new FoodItem(601, "Pepperoni Slice", 5.00, 2.99, 8, "10:00 PM", 0,
                    "Classic pepperoni slice"));
            items.add(new FoodItem(602, "Margherita Pizza", 18.00, 12.00, 1, "10:00 PM", 0,
                    "Whole 12-inch margherita"));
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