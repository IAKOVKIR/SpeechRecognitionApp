package com.example.audiochatbot.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.audiochatbot.database.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Update
    fun update(user: User)

    @Query("DELETE FROM USER")
    fun clear()

    @Query("SELECT COUNT(*) FROM USER LIMIT 1")
    fun getOne(): Int

    @Query("SELECT * FROM USER ORDER BY UserID DESC")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM USER WHERE UserID = :key AND Password = :password AND Position = :char")
    fun getUser(key: Int, password: String, char: Char): User?

    @Query("SELECT COUNT(*) FROM USER")
    fun getTotal(): Int

    @Query("DELETE FROM USER WHERE UserID IN (:idList)")
    fun deleteUsers(idList: List<Int>)

    @Query("SELECT * from USER WHERE userId = :key")
    fun getLiveUserWithId(key: Int): LiveData<User>

    @Query("SELECT * from USER WHERE userId = :key")
    fun getUserWithId(key: Int): User

    @Query("SELECT * FROM USER ORDER BY UserID DESC LIMIT 1")
    fun getLastUser(): User
}