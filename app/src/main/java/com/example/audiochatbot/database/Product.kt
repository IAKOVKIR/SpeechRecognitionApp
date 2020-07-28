package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "PRODUCT",
    indices = [Index(value = ["ProductID"], unique = true)],
    foreignKeys = [ForeignKey(entity = Business::class, parentColumns =
    ["BusinessID"], childColumns = ["BusinessID"], onDelete = ForeignKey.CASCADE)])
data class Product(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ProductID")
    var productId: Int,

    @ColumnInfo(name = "BusinessID")
    var businessId: Int,

    @ColumnInfo(name = "Name")
    var name: String,

    @ColumnInfo(name = "SmallUnitName")
    var smallUnitName: String,

    @ColumnInfo(name = "BigUnitName")
    var bigUnitName: String,

    @ColumnInfo(name = "Quantity")
    var quantity: Int,

    @ColumnInfo(name = "Conversion")
    var conversion: String,

    @ColumnInfo(name = "Price")
    var price: Float,

    @ColumnInfo(name = "Sale")
    var sale: Int
) {
    constructor() :
            this(-1, -1, "", "", "", 0, "", 0.0F, 0)
}