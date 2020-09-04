package com.example.audiochatbot.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(primaryKeys = ["UserID", "DeliveryProductID"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = DeliveryProduct::class, parentColumns = ["DeliveryProductID"],
            childColumns = ["DeliveryProductID"], onDelete = ForeignKey.CASCADE)])
data class DeliveryStatus (
    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "DeliveryProductID")
    var deliveryProductId: Int,

    @ColumnInfo(name = "Status")
    var status: String,

    @ColumnInfo(name = "Date")
    var date: String,

    @ColumnInfo(name = "Time")
    var time: String
)