package com.example.audiochatbot.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.audiochatbot.database.Store

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStore(store: Store)

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC")
    fun getAllStores(): LiveData<List<Store>>

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC LIMIT 1")
    fun getLastStore(): Store

    @Query("SELECT BusinessID FROM STORE INNER JOIN ASSIGNED_STORE ON STORE.StoreID = ASSIGNED_STORE.StoreID WHERE ASSIGNED_STORE.UserID = :adminId")
    fun getAdminsBusinessId(adminId: Int): Int
}