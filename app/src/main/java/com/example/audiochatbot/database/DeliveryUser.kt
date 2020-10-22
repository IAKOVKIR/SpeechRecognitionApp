package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "DELIVERY_USER",
    indices = [Index(value = ["DeliveryUserID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Delivery::class, parentColumns = ["DeliveryID"],
            childColumns = ["DeliveryID"], onDelete = ForeignKey.CASCADE)])
data class DeliveryUser (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DeliveryUserID")
    var deliveryUserId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "DeliveryID")
    var deliveryId: Int,

    @ColumnInfo(name = "DateModified")
    var dateModified: String,

    @ColumnInfo(name = "TimeModified")
    var timeModified: String
)