package com.anurag.foodrunner.database

import androidx.room.*


@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val resId: String,
    @ColumnInfo(name = "food_items") val foodItems: String
)