package com.example.audiochatbot.database.models

import androidx.room.*

@Entity(tableName = "DELIVERY",
    indices = [Index(value = ["DeliveryID"], unique = true)],
    foreignKeys = [ForeignKey(entity = Store::class, parentColumns = [
        "StoreID"], childColumns = ["StoreID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = [
            "UserID"], childColumns = ["UserID"], onDelete = ForeignKey.CASCADE)])
data class Delivery (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DeliveryID")
    var deliveryId: Int,

    @ColumnInfo(name = "StoreID")
    var storeId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "Status")
    var status: String,

    @ColumnInfo(name = "DateModified")
    var dateModified: String,

    @ColumnInfo(name = "TimeModified")
    var timeModified: String
)