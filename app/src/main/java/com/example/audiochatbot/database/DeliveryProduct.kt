package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "DELIVERY_PRODUCT",
    indices = [Index(value = ["DeliveryProductID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = Delivery::class, parentColumns = ["DeliveryID"],
            childColumns = ["DeliveryID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = AssignedProduct::class, parentColumns = ["AssignedProductID"],
            childColumns = ["AssignedProductID"], onDelete = ForeignKey.CASCADE)])
data class DeliveryProduct (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DeliveryProductID")
    var deliveryProductId: Int,

    @ColumnInfo(name = "DeliveryID")
    var deliveryId: Int,

    @ColumnInfo(name = "AssignedProductID")
    var assignedProductId: Int,

    @ColumnInfo(name = "SmallUnitQuantity")
    var smallUnitQuantity: Int,

    @ColumnInfo(name = "BigUnitQuantity")
    var bigUnitQuantity: Int
)