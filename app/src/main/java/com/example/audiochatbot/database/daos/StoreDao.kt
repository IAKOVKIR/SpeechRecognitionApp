package com.example.audiochatbot.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.database.User

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStore(store: Store)

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC")
    fun getAllStores(): LiveData<List<Store>>

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC LIMIT 1")
    fun getLastStore(): Store
}