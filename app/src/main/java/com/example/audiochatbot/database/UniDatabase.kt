package com.example.audiochatbot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Business::class, Store::class, AssignedUser::class, Product::class,
AssignedProduct::class, DiscardedItem::class, Delivery::class, DeliveryUser::class, DeliveryProduct::class,
InventoryCount::class, CashOperation::class],
    version = 1, exportSchema = false)
abstract class UniDatabase: RoomDatabase() {

    abstract val userDao: UserDao

    companion object {

        private val businesses = listOf(
            Business(1, "Walmart", "King St",
            "Melbourne", "Victoria", "0493959766", "Walmart@gmail.com",
            3096),
            Business(2, "7-eleven", "Malcolm St",
                "Melbourne", "Victoria", "0493959556", "seveneleven@gmail.com",
                3093)
        )

        private val stores = listOf(
            Store(1, 1, "Chapel St", "Melbourne",
            "Victoria", "0495673253", 3183, 3000F),
            Store(2, 1, "Greeves St", "Melbourne",
                "Victoria", "0498745535", 3182, 3000F)
        )

        private val users = listOf(
            User(1, 1, "Jay", "Calingacion", "jay@gmail.com",
                "0498629801", "12345678", 'A'),
            User(2, 1,  "Jamie", "Simon", "jamie@gmail.com",
                "0498629802", "12345678", 'E'),
            User(3, 1,  "Kirill", "Iakovlev", "kirill@gmail.com",
                "0498629803", "12345678", 'E'),
            User(4, 1,  "Chris", "Paul", "chris@gmail.com",
            "0498629804", "12345678", 'E'),
            User(5, 1,   "Mike", "Miller", "mike@gmail.com",
                "0498629805", "12345678", 'E'),
            User(6, 2,   "Lavar", "Ball", "ball@gmail.com",
                "0498629806", "12345678", 'E'),
            User(7, 1,   "Saddam", "Hussein", "saddam@gmail.com",
                "0492121396", "12345678", 'D'),
        )

        private val assignUsers = listOf(
            AssignedUser(1, 2, 1, 1, "18/07/2020", "13:00"),
            AssignedUser(2, 3, 1, 1, "18/07/2020", "13:00"),
            AssignedUser(3, 4, 1, 1, "18/07/2020", "13:00")
        )

        private val products = listOf(
            Product(1, 1, "Coca-Cola", "bottle", "6 pack", "1:6", 1.5F),
            Product(2, 1, "Snickers", "bar", "box", "1:16", 1F),
            Product(3, 1, "Bundaberg Ginger Beer", "bottle", "4 pack", "1:4", 2.6F),
            Product(4, 1, "Schweppes Lemon", "bottle", "4 pack", "1:4", 2F),
            Product(5, 1, "Kirks Ginger Beer", "bottle", "10 pack", "1:10", 1.85F),
            Product(6, 1, "Pepsi Max", "can", "10 pack", "1:10", 1F),
            Product(7, 1, "Tim Tam", "pack", "family box", "1:20", 2.8F),
            Product(8, 1, "Doritos Supreme", "pack", "box", "1:6", 3.2F)
        )

        private val assignProducts = listOf(
            AssignedProduct(1, 1, 1, 30, 0, "18/07/2020", "13:00"),
            AssignedProduct(2, 2, 1, 23, 5, "18/07/2020", "13:00"),
            AssignedProduct(3, 3, 1, 15, 0, "18/07/2020", "13:00"),
            AssignedProduct(4, 4, 1, 20, 10, "18/07/2020", "13:00"),
            AssignedProduct(5, 5, 1, 25, 0, "18/07/2020", "13:00"),
            AssignedProduct(6, 6, 2, 40, 0, "18/07/2020", "13:00"),
            AssignedProduct(7, 7, 2, 50, 10, "18/07/2020", "13:00"),
            AssignedProduct(8, 8, 2, 25, 0, "18/07/2020", "13:00")
        )

        private val discardedItems = listOf(
            DiscardedItem(1, 1, 1, 2, "18/07/2020", "13:00"),
            DiscardedItem(2, 4, 1, 3, "18/07/2020", "13:00")
        )

        private val deliveries = listOf(
            Delivery(1, 1, "Waiting", "18/07/2020", "13:00"),
            Delivery(2, 1, "Delivered", "19/07/2020", "12:00"))

        private val deliveryItems = listOf(
            DeliveryProduct(1, 1, 10, 4, "not available"),
            DeliveryProduct(1, 3, 7, 2, "not available"),
            DeliveryProduct(1, 4, 14, 3, "not available"),
            DeliveryProduct(2, 1, 10, 4, "not available"),
            DeliveryProduct(2, 2, 7, 2, "not available"),
            DeliveryProduct(2, 5, 14, 3, "not available")
        )

        private val inventories = listOf(
            InventoryCount(1, 1, 1, 700F, 700F, "18/07/2020", "12:00"),
            InventoryCount(2, 1, 1, 860F, 850F, "19/07/2020", "12:00"),
        )

        private val cashOperations = listOf(
            CashOperation(1, 1, 1, 3000F, true, "20/08/2020", "13:00"),
            CashOperation(1, 3, 1, 600F, false, "20/08/2020", "13:00"),
            CashOperation(1, 2, 1, 600F, false, "20/08/2020", "13:00"),
            CashOperation(1, 4, 1, 600F, false, "20/08/2020", "13:00"),
        )

        @Volatile
        private var INSTANCE: UniDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): UniDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UniDatabase::class.java,
                        "uni_database")
                        .fallbackToDestructiveMigration()
                        .addCallback(DbCallback(scope))
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }

        private class DbCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDb(database.userDao)
                    }
                }
            }
        }

        private fun populateDb(userDao: UserDao) {
            userDao.insertBusinesses(businesses)
            userDao.insertStores(stores)
            userDao.insertUsers(users)
            userDao.assignUsers(assignUsers)
            userDao.insertProducts(products)
            userDao.assignProducts(assignProducts)
            userDao.discardItems(discardedItems)
            userDao.insertDeliveries(deliveries)
            userDao.insertDeliveryProducts(deliveryItems)
            userDao.insertInventoryCount(inventories)
            userDao.insertCashOperations(cashOperations)
        }
    }
}