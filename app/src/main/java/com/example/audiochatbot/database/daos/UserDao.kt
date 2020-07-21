package com.example.audiochatbot.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.audiochatbot.database.*

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

    @Query("SELECT * FROM USER INNER JOIN ASSIGNED_USER ON USER.UserID = ASSIGNED_USER.UserID WHERE StoreID = :storeId ORDER BY UserID DESC")
    fun getAllUsersWithStoreID(storeId: Int): LiveData<List<User>>

    @Query("SELECT * FROM USER WHERE UserID = :key AND Password = :password AND Position = :char")
    fun getUser(key: Int, password: String, char: Char): User?

    @Query("SELECT COUNT(*) FROM USER")
    fun getTotal(): Int

    @Query("DELETE FROM USER WHERE UserID = :userId")
    fun deleteUserRecord(userId: Int)

    @Query("DELETE FROM USER WHERE UserID IN (:idList)")
    fun deleteUsers(idList: List<Int>)

    @Query("SELECT * from USER WHERE userId = :key")
    fun getLiveUserWithId(key: Int): LiveData<User>

    @Query("SELECT * from USER WHERE userId = :key")
    fun getUserWithId(key: Int): User

    @Query("SELECT * FROM USER ORDER BY UserID DESC LIMIT 1")
    fun getLastUser(): User


    //Assigned User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun assignUser(newAssignUser: AssignedUser)

    @Query("SELECT AssignedUserID FROM ASSIGNED_USER ORDER BY AssignedUserID DESC LIMIT 1")
    fun getLastAssignedUserId(): Int

    @Query("SELECT COUNT(*) FROM ASSIGNED_USER WHERE UserID = :userId AND StoreID = :storeId")
    fun ifUserAssigned(userId: Int, storeId: Int): Int

    //Store

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStore(store: Store)

    @Update
    fun updateStore(store: Store)

    @Query("DELETE FROM STORE WHERE StoreID = :storeId")
    fun deleteStoreRecord(storeId: Int)

    @Query("SELECT * FROM STORE INNER JOIN BUSINESS ON STORE.BusinessID = BUSINESS.BusinessID INNER JOIN ASSIGNED_ADMIN ON BUSINESS.BusinessID = ASSIGNED_ADMIN.BusinessID ORDER BY StoreID DESC")
    fun getAllAdminStores(): LiveData<List<Store>>

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC LIMIT 1")
    fun getLastStore(): Store

    @Query("SELECT * FROM STORE WHERE storeId = :key")
    fun getStoreWithId(key: Int): Store


    //Business
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBusiness(business: Business)

    @Query("SELECT BusinessID FROM ASSIGNED_ADMIN WHERE UserID = :adminId")
    fun getAdminsBusinessId(adminId: Int): Int


    //Delivery User
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDeliveryUser(deliveryUser: DeliveryUser)

    @Query("SELECT * FROM DELIVERY_USER WHERE DeliveryUserID = :key AND Password = :password")
    fun getDeliveryUser(key: Int, password: String): DeliveryUser?

    @Query("SELECT COUNT(*) FROM DELIVERY_USER")
    fun getDeliveryUserTotal(): Int


    //Assign Admin
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun assignAdmin(assignedAdmin: AssignedAdmin)
}