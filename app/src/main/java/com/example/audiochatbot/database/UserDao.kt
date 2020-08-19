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

    @Query("SELECT * FROM USER WHERE BusinessID = :businessId ORDER BY UserID DESC")
    fun getAllLiveUsersWithBusinessId(businessId: Int): LiveData<List<User>>

    @Query("SELECT * FROM USER INNER JOIN ASSIGNED_USER ON USER.UserID = ASSIGNED_USER.UserID WHERE StoreID = :storeId ORDER BY UserID DESC")
    fun getAllUsersLiveWithStoreID(storeId: Int): LiveData<List<User>>

    @Query("SELECT USER.UserID, BusinessID, FirstName, LastName, Email, PhoneNumber, Password, Position FROM USER LEFT JOIN ASSIGNED_USER ON USER.UserID = ASSIGNED_USER.UserID WHERE ASSIGNED_USER.StoreID IS NOT :storeId AND USER.BusinessID = :businessId AND USER.Position = :pos ORDER BY USER.UserID DESC")
    fun getAllUsersLiveWithoutStoreID(storeId: Int, businessId: Int, pos: Char): LiveData<List<User>>

    @Query("SELECT * FROM USER INNER JOIN ASSIGNED_USER ON USER.UserID = ASSIGNED_USER.UserID WHERE StoreID = :storeId ORDER BY UserID DESC")
    fun getAllUsersWithStoreID(storeId: Int): List<User>

    @Query("SELECT * FROM USER WHERE (FirstName LIKE :line OR LastName LIKE :line) AND BusinessID = :businessId ORDER BY UserID DESC")
    fun getAllUsersWithString(line: String, businessId: Int): List<User>

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

    @Query("DELETE FROM ASSIGNED_USER WHERE UserID = :userId AND StoreID = :storeId")
    fun removeUserFromStore(userId: Int, storeId: Int)

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

    @Update
    fun update(product: Product)

    @Query("SELECT * FROM PRODUCT WHERE BusinessID = :businessId ORDER BY ProductID DESC")
    fun getAllProductsWithBusinessId(businessId: Int): List<Product>

    @Query("SELECT * FROM PRODUCT WHERE BusinessID = :businessId ORDER BY ProductID DESC")
    fun getAllLiveProductsWithBusinessId(businessId: Int): LiveData<List<Product>>

    @Query("SELECT * FROM PRODUCT WHERE Name LIKE :line AND BusinessID = :businessId ORDER BY ProductID DESC")
    fun getAllProductsWithString(line: String, businessId: Int): List<Product>

    @Query("SELECT PRODUCT.ProductID, BusinessID, Name, SmallUnitName, BigUnitName, Quantity, Conversion, Price, Sale FROM PRODUCT LEFT JOIN ASSIGNED_PRODUCT ON PRODUCT.ProductID = ASSIGNED_PRODUCT.ProductID WHERE ASSIGNED_PRODUCT.StoreID IS NOT :storeId AND PRODUCT.BusinessID = :businessId ORDER BY PRODUCT.ProductID DESC")
    fun getAllProductsLiveWithoutStoreID(storeId: Int, businessId: Int): LiveData<List<Product>>

    @Query("SELECT * FROM PRODUCT INNER JOIN ASSIGNED_PRODUCT ON PRODUCT.ProductID = ASSIGNED_PRODUCT.ProductID WHERE StoreID = :storeId ORDER BY ProductID DESC")
    fun getAllProductsLiveWithStoreID(storeId: Int): LiveData<List<Product>>

    @Query("SELECT * FROM PRODUCT WHERE ProductID = :key")
    fun getProductWithId(key: Int): Product

    @Query("SELECT COUNT(*) FROM PRODUCT WHERE ProductID = :key")
    fun getProductIdWithId(key: Int): Int

    @Query("SELECT * FROM PRODUCT ORDER BY ProductID DESC LIMIT 1")
    fun getLastProduct(): Product

    @Query("DELETE FROM PRODUCT WHERE ProductID = :productId")
    fun deleteProductRecord(productId: Int)


    //Assigned Product
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun assignProduct(newAssignProduct: AssignedProduct)

    @Query("DELETE FROM ASSIGNED_PRODUCT WHERE ProductID = :productId AND StoreID = :storeId")
    fun removeProductFromStore(productId: Int, storeId: Int)

    @Query("SELECT COUNT(*) FROM ASSIGNED_PRODUCT WHERE ProductID = :productId AND StoreID = :storeId")
    fun ifProductAssigned(productId: Int, storeId: Int): Int

    @Query("SELECT AssignedProductID FROM ASSIGNED_PRODUCT ORDER BY AssignedProductID DESC LIMIT 1")
    fun getLastAssignedProductId(): Int

    @Query("SELECT SUM(Quantity) FROM ASSIGNED_PRODUCT WHERE ProductID = :productId")
    fun totalProductQuantity(productId: Int): Int
}