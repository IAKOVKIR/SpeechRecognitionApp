package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "ASSIGNED_ADMIN",
    indices = [Index(value = ["AssignedAdminID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["AdministratorID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Business::class, parentColumns = ["BusinessID"],
            childColumns = ["BusinessID"], onDelete = ForeignKey.CASCADE)])
data class AssignedAdmin(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "AssignedAdminID")
    var assignedAdminId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "AdministratorID")
    var administratorId: Int,

    @ColumnInfo(name = "BusinessID")
    var businessId: Int,

    @ColumnInfo(name = "Date")
    var date: String,

    @ColumnInfo(name = "Time")
    var time: String
)