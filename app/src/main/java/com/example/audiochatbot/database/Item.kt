package com.example.audiochatbot.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "ITEM",
    primaryKeys = ["AssignedProductID", "InventoryCountID"],
    foreignKeys = [
        ForeignKey(entity = AssignedProduct::class, parentColumns = ["AssignedProductID"],
            childColumns = ["AssignedProductID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = InventoryCount::class, parentColumns = ["InventoryCountID"],
            childColumns = ["InventoryCountID"], onDelete = ForeignKey.CASCADE)])
data class Item (
    @ColumnInfo(name = "AssignedProductID")
    var productId: Int,

    @ColumnInfo(name = "InventoryCountID")
    var inventoryCountId: Int,

    @ColumnInfo(name = "SmallUnitQuantity")
    var smallUnitQuantity: Int,

    @ColumnInfo(name = "BigUnitQuantity")
    var bigUnitQuantity: Int
)