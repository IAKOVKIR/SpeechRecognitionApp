package com.example.audiochatbot.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeliveryUserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDeliveryUser(deliveryUser: DeliveryUser)

    //@Insert
    //fun insertStore(store: Store)

    // @Insert
    //fun insertAssignedStore(assignedStore: AssignedStore)

    //@Insert
    //fun insertBusiness(business: Business)

    @Query("SELECT * FROM DELIVERY_USER WHERE DeliveryUserID = :key AND Password = :password")
    fun getDeliveryUser(key: Int, password: String): DeliveryUser?

    @Query("SELECT COUNT(*) FROM DELIVERY_USER")
    fun getDeliveryUserTotal(): Int
}