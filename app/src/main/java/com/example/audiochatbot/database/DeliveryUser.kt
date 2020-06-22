package com.example.audiochatbot.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "DELIVERY_USER",
    indices = [Index(value = ["DeliveryUserID", "Email", "PhoneNumber"], unique = true)])
data class DeliveryUser (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DeliveryUserID")
    val deliveryUserId: Int,

    @ColumnInfo(name = "FirstName")
    val firstName: String,

    @ColumnInfo(name = "LastName")
    val lastName: String,

    @ColumnInfo(name = "Email")
    val email: String,

    @ColumnInfo(name = "Password")
    val password: String,

    @ColumnInfo(name = "PhoneNumber")
    val phoneNumber: String
)