package com.project.foodwastagereductionapp.data;

/** A restaurant plus its current surplus food summary, for the search list. */
public final class RestaurantListing {
    public final Restaurant restaurant;

    // total quantity still available
    public final int itemsLeft;

    // latest pickup time, may be null
    public final String availableUntil;
    public RestaurantListing(Restaurant restaurant, int itemsLeft, String availableUntil) {
        this.restaurant = restaurant;
        this.itemsLeft = itemsLeft;
        this.availableUntil = availableUntil;
    }
}
