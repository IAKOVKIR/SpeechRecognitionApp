package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "USER",
    indices = [Index(value = ["UserID", "Email", "PhoneNumber"], unique = true)],
    foreignKeys = [ForeignKey(entity = Business::class, parentColumns = [
        "BusinessID"], childColumns = ["BusinessID"], onDelete = ForeignKey.CASCADE)])
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "UserID")
    var userId: Int = 0,

    @ColumnInfo(name = "BusinessID")
    var businessId: Int,

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
) {
    constructor() :
            this(-1, -1, "", "", "", "", "", 'L')
    constructor(firstName: String, lastName: String, email: String, phoneNumber: String, password: String, position: Char) :
            this(-1, -1, firstName, lastName, email, phoneNumber, password, position)
}