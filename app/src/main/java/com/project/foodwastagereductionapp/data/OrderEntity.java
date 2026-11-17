package com.project.foodwastagereductionapp.data;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long customerId;
    public long restaurantId;
    public String orderNumber;
    public String customerName;
    public String foodItems;
    public String date;
    public String time;
    public int itemCount;
    public double total;
    public String status;
    public String pickupMethod;
}
