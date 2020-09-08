package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "DELIVERY_PRODUCT",
    primaryKeys = ["DeliveryID", "ProductID"],
    foreignKeys = [
        ForeignKey(entity = Delivery::class, parentColumns = ["DeliveryID"],
            childColumns = ["DeliveryID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Product::class, parentColumns = ["ProductID"],
            childColumns = ["ProductID"], onDelete = ForeignKey.CASCADE)])
data class DeliveryProduct (
    @ColumnInfo(name = "DeliveryID")
    var deliveryId: Int,

    @ColumnInfo(name = "ProductID")
    var productId: Int,

    @ColumnInfo(name = "SmallUnitQuantity")
    var smallUnitQuantity: Int,

    @ColumnInfo(name = "BigUnitQuantity")
    var bigUnitQuantity: Int,

    @ColumnInfo(name = "Status")
    var status: String
)