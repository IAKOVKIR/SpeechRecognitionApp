package com.example.audiochatbot.database

import androidx.lifecycle.LiveData
import androidx.room.*

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

    @Query("SELECT COUNT(*) FROM USER WHERE UserID = :userId")
    fun ifUserExist(userId: Int): Int

    @Query("SELECT Position FROM USER WHERE UserID = :userId")
    fun getUserPosition(userId: Int): Char

    @Query("SELECT * FROM USER ORDER BY UserID DESC")
    fun getAllLiveUsers(): LiveData<List<User>>

    @Query("SELECT * FROM USER ORDER BY UserID DESC")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM USER WHERE BusinessID = :businessId ORDER BY UserID DESC")
    fun getAllUsersWithBusinessId(businessId: Int): List<User>

    @Query("SELECT * FROM USER INNER JOIN ASSIGNED_USER ON USER.UserID = ASSIGNED_USER.UserID WHERE StoreID = :storeId ORDER BY UserID DESC")
    fun getAllUsersLiveWithStoreID(storeId: Int): LiveData<List<User>>

    @Query("SELECT * FROM USER INNER JOIN ASSIGNED_USER ON USER.UserID = ASSIGNED_USER.UserID WHERE StoreID = :storeId ORDER BY UserID DESC")
    fun getAllUsersWithStoreID(storeId: Int): List<User>

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

    @Query("DELETE FROM ASSIGNED_USER WHERE UserID = :userId")
    fun removeUserFromStore(userId: Int)

    //Store

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStore(store: Store)

    @Update
    fun updateStore(store: Store)

    @Query("DELETE FROM STORE WHERE StoreID = :storeId")
    fun deleteStoreRecord(storeId: Int)

    @Query("SELECT * FROM STORE INNER JOIN USER ON STORE.BusinessID = USER.BusinessID WHERE USER.UserID = :adminId ORDER BY StoreID DESC")
    fun getAllAdminStores(adminId: Int): LiveData<List<Store>>

    @Query("SELECT * FROM STORE ORDER BY StoreID DESC LIMIT 1")
    fun getLastStore(): Store

    @Query("SELECT * FROM STORE WHERE storeId = :key")
    fun getStoreWithId(key: Int): Store

    @Query("SELECT COUNT(*) FROM STORE WHERE StoreID = :storeId AND BusinessID = :businessId")
    fun ifStoreBelongToBusiness(storeId: Int, businessId: Int): Int


    //Business
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBusiness(business: Business)

    @Query("SELECT BusinessID FROM USER WHERE UserID = :adminId")
    fun getAdminsBusinessId(adminId: Int): Int


    //Delivery User
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDeliveryUser(deliveryUser: DeliveryUser)

    @Query("SELECT * FROM DELIVERY_USER WHERE DeliveryUserID = :key AND Password = :password")
    fun getDeliveryUser(key: Int, password: String): DeliveryUser?

    @Query("SELECT COUNT(*) FROM DELIVERY_USER")
    fun getDeliveryUserTotal(): Int


    //Product
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product)
}