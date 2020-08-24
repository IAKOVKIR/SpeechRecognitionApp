package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "DISCARDED_ITEM",
    indices = [Index(value = ["DiscardedItemID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = AssignedProduct::class, parentColumns = ["AssignedProductID"],
            childColumns = ["AssignedProductID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE)])
data class DiscardedItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DiscardedItemID")
    var discardedItemId: Int,

    @ColumnInfo(name = "AssignedProductID")
    var assignedProductId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "Quantity")
    var quantity: Int,

    @ColumnInfo(name = "Condition")
    var condition: String,

    @ColumnInfo(name = "Date")
    var date: String,

    @ColumnInfo(name = "Time")
    var time: String
)