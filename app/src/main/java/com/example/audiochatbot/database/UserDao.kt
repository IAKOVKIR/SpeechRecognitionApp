package com.example.audiochatbot.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(user: User)

    //@Insert
    //fun insertStore(store: Store)

   // @Insert
    //fun insertAssignedStore(assignedStore: AssignedStore)

    //@Insert
    //fun insertBusiness(business: Business)

    @Query("DELETE FROM USER")
    fun clear()

    @Query("SELECT * FROM USER WHERE UserID = :key AND Password = :password AND Position = :char")
    fun getUser(key: Int, password: String, char: Char): User?

    @Query("SELECT COUNT(*) FROM USER")
    fun getTotal(): Int
}