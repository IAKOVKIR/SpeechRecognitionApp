package com.example.audiochatbot.database.models

import androidx.room.*

@Entity(tableName = "DELIVERY_PRODUCT_STATUS",
    primaryKeys = ["DeliveryProductID", "UserID"],
    foreignKeys = [
        ForeignKey(entity = DeliveryProduct::class, parentColumns = ["DeliveryProductID"],
            childColumns = ["DeliveryProductID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE)])
data class DeliveryProductStatus (
    @ColumnInfo(name = "DeliveryProductID")
    var deliveryProductId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "Status")
    var status: String,

    @ColumnInfo(name = "DateModified")
    var dateModified: String,

    @ColumnInfo(name = "TimeModified")
    var timeModified: String
)