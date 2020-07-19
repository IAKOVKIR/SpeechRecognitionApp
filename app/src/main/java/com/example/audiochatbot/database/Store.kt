package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "STORE",
    indices = [Index(value = ["StoreID"], unique = true)],
    foreignKeys = [ForeignKey(entity = Business::class, parentColumns = [
        "BusinessID"], childColumns = ["BusinessID"], onDelete = ForeignKey.CASCADE)])
data class Store(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "StoreID")
    var storeId: Int,

    @ColumnInfo(name = "BusinessID")
    var businessId: Int,

    @ColumnInfo(name = "Street")
    var street: String,

    @ColumnInfo(name = "City")
    var city: String,

    @ColumnInfo(name = "State")
    var state: String,

    @ColumnInfo(name = "PhoneNumber")
    var phoneNumber: String,

    @ColumnInfo(name = "zip_code")
    var zip_code: Int,

    @ColumnInfo(name = "CashOnHand")
    var cashOnHand: Float
) {
    constructor() :
            this(-1, -1,"", "", "", "", -1, 0F)
}