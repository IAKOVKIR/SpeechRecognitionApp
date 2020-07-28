package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "ASSIGNED_PRODUCT",
    indices = [Index(value = ["AssignedProductID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = Product::class, parentColumns = ["ProductID"],
            childColumns = ["ProductID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Store::class, parentColumns = ["StoreID"],
            childColumns = ["StoreID"], onDelete = ForeignKey.CASCADE)])
data class AssignedProduct(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "AssignedProductID")
    var assignedProductId: Int,

    @ColumnInfo(name = "ProductID")
    var productId: Int,

    @ColumnInfo(name = "StoreID")
    var storeId: Int,

    @ColumnInfo(name = "Date")
    var date: String,

    @ColumnInfo(name = "Time")
    var time: String
)