package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "DELIVERY_PRODUCT",
    indices = [Index(value = ["DeliveryProductID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = Delivery::class, parentColumns = ["DeliveryID"],
            childColumns = ["DeliveryID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Product::class, parentColumns = ["ProductID"],
            childColumns = ["ProductID"], onDelete = ForeignKey.CASCADE)])
data class DeliveryProduct (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DeliveryProductID")
    var deliveryProductId: Int,

    @ColumnInfo(name = "DeliveryID")
    var deliveryId: Int,

    @ColumnInfo(name = "ProductID")
    var productId: Int,

    @ColumnInfo(name = "SmallUnitQuantity")
    var smallUnitQuantity: Int,

    @ColumnInfo(name = "BigUnitQuantity")
    var bigUnitQuantity: Int
)