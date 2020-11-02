package com.example.audiochatbot.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "BUSINESS",
    indices = [Index(value = ["BusinessID"],
    unique = true)])
data class Business(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BusinessID")
    var businessId: Int,

    @ColumnInfo(name = "Name")
    var name: String,

    @ColumnInfo(name = "Street")
    var street: String,

    @ColumnInfo(name = "City")
    var city: String,

    @ColumnInfo(name = "State")
    var state: String,

    @ColumnInfo(name = "PhoneNumber")
    var phoneNumber: String,

    @ColumnInfo(name = "Email")
    var email: String,

    @ColumnInfo(name = "zip_code")
    var zip_code: Int
)