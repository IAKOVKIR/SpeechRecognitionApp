package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "INVENTORY_COUNT",
    primaryKeys = ["StoreID", "UserID"],
    foreignKeys = [
        ForeignKey(entity = Store::class, parentColumns = ["StoreID"],
            childColumns = ["StoreID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE)])
class InventoryCount (
    @ColumnInfo(name = "StoreID")
    var storeId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "ExpectedEarnings")
    var expectedEarnings: Float,

    @ColumnInfo(name = "TotalEarnings")
    var totalEarnings: Float,

    @ColumnInfo(name = "Date")
    var date: String,

    @ColumnInfo(name = "Time")
    var time: String
)