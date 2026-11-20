package com.project.foodwastagereductionapp.data;

public final class Restaurant {
    public final long id;
    public final long managerId;
    public final String name;
    public final String city;
    public final String province;
    public final String postalCode;
    public final String phone;
    public final String cuisineType;
    public final String description;
    public final String imageUri;
    public final Double latitude;  // null until the address has been turned into map coordinates
    public final Double longitude;

    public Restaurant(
            long id,
            long managerId,
            String name,
            String city,
            String province,
            String postalCode,
            String phone,
            String cuisineType,
            String description,
            String imageUri
    ) {
        this(id, managerId, name, city, province, postalCode, phone, cuisineType, description,
                imageUri, null, null);
    }

    public Restaurant(
            long id,
            long managerId,
            String name,
            String city,
            String province,
            String postalCode,
            String phone,
            String cuisineType,
            String description,
            String imageUri,
            Double latitude,
            Double longitude
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.managerId = managerId;
        this.name = name;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.phone = phone;
        this.cuisineType = cuisineType;
        this.description = description;
        this.imageUri = imageUri;
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}