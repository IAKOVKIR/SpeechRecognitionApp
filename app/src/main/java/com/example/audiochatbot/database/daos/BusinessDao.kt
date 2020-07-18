package com.example.audiochatbot.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.audiochatbot.database.Business

@Dao
interface BusinessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBusiness(business: Business)
}