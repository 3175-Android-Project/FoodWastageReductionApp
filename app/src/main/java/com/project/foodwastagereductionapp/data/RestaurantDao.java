package com.project.foodwastagereductionapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
interface RestaurantDao {
    @Insert
    long insert(RestaurantEntity restaurant);

    @Query("SELECT * FROM restaurants WHERE manager_id = :managerId LIMIT 1")
    RestaurantEntity findByManagerId(long managerId);

    @Query("UPDATE restaurants SET name = :name, city = :city, province = :province, " +
            "postal_code = :postalCode, phone = :phone, cuisine_type = :cuisineType, " +
            "description = :description, image_uri = :imageUri WHERE manager_id = :managerId")
    int update(long managerId, String name, String city, String province, String postalCode,
               String phone, String cuisineType, String description, String imageUri);

    @Query("SELECT COUNT(*) FROM restaurants")
    int count();

    @Query("SELECT * FROM restaurants WHERE name LIKE :like OR city LIKE :like OR province LIKE :like OR cuisine_type LIKE :like ORDER BY name COLLATE NOCASE")
    java.util.List<RestaurantEntity> search(String like);

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    RestaurantEntity findById(long id);
}
