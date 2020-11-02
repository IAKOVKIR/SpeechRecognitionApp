package com.example.audiochatbot.database.models

import androidx.room.*

@Entity(tableName = "CASH_OPERATION",
    indices = [Index(value = ["CashOperationID"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["UserID"],
            childColumns = ["UserID"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Store::class, parentColumns = ["StoreID"],
            childColumns = ["StoreID"], onDelete = ForeignKey.CASCADE)])
data class CashOperation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CashOperationID")
    var cashOperationId: Int,

    @ColumnInfo(name = "UserID")
    var userId: Int,

    @ColumnInfo(name = "StoreID")
    var storeId: Int,

    @ColumnInfo(name = "Amount")
    var amount: Float,

    @ColumnInfo(name = "OperationType")
    var operationType: Boolean, //true - Deposit, false - Withdraw

    @ColumnInfo(name = "Date")
    var date: String,

    @ColumnInfo(name = "Time")
    var time: String
)