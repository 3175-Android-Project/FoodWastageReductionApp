package com.project.foodwastagereductionapp.ui;

import android.content.Context;

import com.project.foodwastagereductionapp.R;

import java.util.Arrays;
import java.util.List;

/** Food category dropdown for Add / Edit Food Item (same bottom sheet as the city picker). */
public final class CategoryPickerBottomSheet {

    public static final List<String> CATEGORIES = Arrays.asList(
            "Meals",
            "Bakery",
            "Sushi",
            "Salads",
            "Sandwiches",
            "Desserts",
            "Drinks",
            "Snacks",
            "Groceries",
            "Other"
    );

    private CategoryPickerBottomSheet() {
    }

    public static void show(Context context, CityPickerBottomSheet.OnCitySelectedListener listener) {
        CityPickerBottomSheet.showOptions(context, R.string.titleSelectCategory, CATEGORIES, listener);
    }
}