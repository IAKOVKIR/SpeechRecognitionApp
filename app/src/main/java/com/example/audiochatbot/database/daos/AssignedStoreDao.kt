package com.example.audiochatbot.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.audiochatbot.database.AssignedStore

@Dao
interface AssignedStoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssignedStore(assignedStore: AssignedStore)
}