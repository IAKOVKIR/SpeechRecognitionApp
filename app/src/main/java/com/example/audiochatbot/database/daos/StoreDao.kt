package com.example.audiochatbot.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.database.User

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStore(store: Store)

    @Update
    fun updateStore(store: Store)

    @Query("DELETE FROM STORE WHERE StoreID = :storeId")
    fun deleteRecord(storeId: Int)

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC")
    fun getAllStores(): LiveData<List<Store>>

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC LIMIT 1")
    fun getLastStore(): Store

    @Query("SELECT * from STORE WHERE storeId = :key")
    fun getStoreWithId(key: Int): Store

    @Query("SELECT BusinessID FROM STORE INNER JOIN ASSIGNED_STORE ON STORE.StoreID = ASSIGNED_STORE.StoreID WHERE ASSIGNED_STORE.UserID = :adminId")
    fun getAdminsBusinessId(adminId: Int): Int
}