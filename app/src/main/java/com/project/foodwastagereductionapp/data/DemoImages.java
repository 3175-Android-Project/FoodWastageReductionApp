package com.project.foodwastagereductionapp.data;

import android.content.Context;
import androidx.annotation.DrawableRes;
import com.project.foodwastagereductionapp.R;

/** Bundled presentation photos. Explicit R references also keep them during resource shrinking. */
final class DemoImages {
    private DemoImages() { }

    static String uri(Context context, @DrawableRes int drawable) {
        return "android.resource://" + context.getPackageName() + "/drawable/"
                + context.getResources().getResourceEntryName(drawable);
    }

    static final RestaurantPhotos[] RESTAURANTS = {
            new RestaurantPhotos("manager@grabbit.demo", "Sushi Barff", R.drawable.img_sushi_restaurant,
                    new String[]{"Salmon oshi", "Tuna tadaki", "Pork gyoza", "Takoyaki"},
                    new int[]{R.drawable.img_sushi_salmon_oshi, R.drawable.img_sushi_tuna_tadaki,
                            R.drawable.img_sushi_pork_gyoza, R.drawable.img_sushi_takoyaki}),
            new RestaurantPhotos("manager2@grabbit.demo", "Italian Trattoria", R.drawable.img_trattoria_restaurant,
                    new String[]{"Margherita pizza", "Bee sting pizza", "Truffle Pasta"},
                    new int[]{R.drawable.img_trattoria_margherita_pizza, R.drawable.img_trattoria_bee_sting_pizza,
                            R.drawable.img_trattoria_truffle_pasta}),
            new RestaurantPhotos("manager3@grabbit.demo", "Burnaby Burger Co.", R.drawable.img_burger_restaurant,
                    new String[]{"Double Smash Burger", "Loaded Cheese Fries"},
                    new int[]{R.drawable.img_burger_smash_burger, R.drawable.img_burger_cheese_fries}),
            new RestaurantPhotos("manager4@grabbit.demo", "Seoul Bistro", R.drawable.img_seoul_restaurant,
                    new String[]{"Crispy Fried Chicken Box", "Bulgogi Rice Bowl"},
                    new int[]{R.drawable.img_seoul_fried_chicken, R.drawable.img_seoul_bulgogi_rice_bowl})
    };

    static final class RestaurantPhotos {
        final String managerEmail, restaurantName;
        final int restaurantImage;
        final String[] foodNames;
        final int[] foodImages;

        RestaurantPhotos(String managerEmail, String restaurantName, int restaurantImage,
                         String[] foodNames, int[] foodImages) {
            this.managerEmail = managerEmail;
            this.restaurantName = restaurantName;
            this.restaurantImage = restaurantImage;
            this.foodNames = foodNames;
            this.foodImages = foodImages;
        }
    }
}
