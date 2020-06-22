package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "USER",
    indices = [Index(value = ["UserID", "Email", "PhoneNumber"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "FirstName")
    var firstName: String,

    @ColumnInfo(name = "LastName")
    var lastName: String,

    @ColumnInfo(name = "Email")
    var email: String,

    @ColumnInfo(name = "PhoneNumber")
    var phoneNumber: String,

    @ColumnInfo(name = "Password")
    var password: String,

    @ColumnInfo(name = "Position")
    var position: Char
)