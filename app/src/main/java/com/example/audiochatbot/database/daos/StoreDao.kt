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
}