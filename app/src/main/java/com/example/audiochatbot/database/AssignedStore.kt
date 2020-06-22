package com.example.audiochatbot.database

import androidx.room.*

@Entity(tableName = "ASSIGNED_STORE",
    indices = [Index(value = ["AssignedStoreID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["AdministratorID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Store::class, parentColumns = ["StoreID"],
            childColumns = ["StoreID"], onDelete = ForeignKey.CASCADE)])
data class AssignedStore(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "AssignedStoreID")
    var assignedStoreId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "AdministratorID")
    var administratorId: Int,

    @ColumnInfo(name = "StoreID")
    var storeId: Int,

    @ColumnInfo(name = "Date")
    var date: String,

    @ColumnInfo(name = "Time")
    var time: String
)