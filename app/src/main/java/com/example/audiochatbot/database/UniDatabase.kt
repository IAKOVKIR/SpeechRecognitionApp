package com.example.audiochatbot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.audiochatbot.database.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A database that stores User, Business, Store, AssignedUser, Product, AssignedProduct,
 * DiscardedItem, Delivery, DeliveryProduct, DeliveryProductStatus, InventoryCount and CashOperation
 * information.
 * And a global method to get access to the database.
 */
@Database(entities = [User::class, Business::class, Store::class, AssignedUser::class, Product::class,
    AssignedProduct::class, DiscardedItem::class, Delivery::class, DeliveryProduct::class,
    DeliveryProductStatus::class, InventoryCount::class, CashOperation::class],
    version = 1, exportSchema = false)
abstract class UniDatabase: RoomDatabase() {

    /**
     * Connects the database to the DAO.
     */
    abstract val userDao: UserDao

    /**
     * Define a companion object, this allows us to add functions on the UniDatabase class.
     */
    companion object {

        private val businesses = listOf(
            Business(1, "Walmart", "King St",
            "Melbourne", "Victoria", "0493959766", "Walmart@gmail.com",
            3096)
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
            User(7, 1,   "Karl", "Malone", "saddam@gmail.com",
                "0492121396", "12345678", 'D'),
        )

        private val assignUsers = listOf(
            AssignedUser(1, 2, 1, 1, "18/07/2020", "13:00"),
            AssignedUser(2, 3, 1, 1, "18/07/2020", "13:00"),
            AssignedUser(3, 4, 1, 1, "18/07/2020", "13:00"),
            AssignedUser(4, 7, 1, 1, "18/07/2020", "13:00"),
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
            DiscardedItem(1, 1, 1, 2, "expired", "18/07/2020", "13:00"),
            DiscardedItem(2, 4, 1, 3, "broken", "18/07/2020", "13:00")
        )

        private val deliveries = listOf(
            Delivery(1, 1, 1, "In Transit", "18/07/2020", "13:00"),
            Delivery(2, 1, 1, "In Transit", "19/07/2020", "12:00")
        )

        private val deliveryItems = listOf(
            DeliveryProduct(1, 1, 1, 10, 4),
            DeliveryProduct(2, 1, 3, 7, 2),
            DeliveryProduct(3, 1, 4, 14, 3),
            DeliveryProduct(4, 2, 1, 10, 4),
            DeliveryProduct(5, 2, 2, 7, 2),
            DeliveryProduct(6, 2, 5, 14, 3)
        )

            /**private val deliveryProductStatuses = listOf(
            DeliveryProductStatus(1, 1, "Accepted", "18/07/2020", "13:00"),
            DeliveryProductStatus(2, 2, "Accepted", "18/07/2020", "13:00")
        )*/

        private val inventories = listOf(
            InventoryCount(1, 1, 1, 700F, 700F, "18/07/2020", "12:00"),
            InventoryCount(2, 1, 1, 860F, 850F, "19/07/2020", "12:00"),
        )

        private val cashOperations = listOf(
            CashOperation(1, 1, 1, 3000F, true, "20/08/2020", "13:00"),
            CashOperation(2, 3, 1, 600F, false, "20/08/2020", "13:00"),
            CashOperation(3, 2, 1, 600F, false, "20/08/2020", "13:00"),
            CashOperation(4, 4, 1, 600F, false, "20/08/2020", "13:00"),
        )

        /**
         * INSTANCE will keep a reference to any database returned via getInstance.
         *
         * This will help us avoid repeatedly initializing the database, which is expensive.
         *
         *  The value of a volatile variable will never be cached, and all writes and
         *  reads will be done to and from the main memory. It means that changes made by one
         *  thread to shared data are visible to other threads.
         */
        @Volatile
        private var INSTANCE: UniDatabase? = null

        /**
         * Helper function to get the database.
         *
         * If a database has already been retrieved, the previous database will be returned.
         * Otherwise, create a new database.
         *
         * This function is threadsafe, and callers should cache the result for multiple database
         * calls to avoid overhead.
         *
         * This is an example of a simple Singleton pattern that takes another Singleton as an
         * argument in Kotlin.
         *
         * @param context The application context Singleton, used to get access to the filesystem.
         * @param scope Defines a scope for new coroutines.
         */
        fun getInstance(context: Context, scope: CoroutineScope): UniDatabase {
            // Multiple threads can ask for the database at the same time, ensure we only initialize
            // it once by using synchronized. Only one thread may enter a synchronized block at a
            // time.
            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                var instance = INSTANCE
                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UniDatabase::class.java,
                        "uni_database")
                        // Wipes and rebuilds instead of migrating if no Migration object.
                        .fallbackToDestructiveMigration()
                        .addCallback(DbCallback(scope))
                        .build()
                    // Assign INSTANCE to the newly created database.
                    INSTANCE = instance
                }
                // Return instance; smart cast to be non-null.
                return instance
            }
        }

        // Executes scripts with Room after database has been created.
        private class DbCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback(){
            //onCreate method is called after database is created
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDb(database.userDao)
                    }
                }
            }
        }

        //inserts all test entries into all the tables
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
            //userDao.insertDeliveryProductStatuses(deliveryProductStatuses)
            userDao.insertInventoryCount(inventories)
            userDao.insertCashOperations(cashOperations)
        }
    }
}