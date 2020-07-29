package com.example.audiochatbot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, DeliveryUser::class, Business::class, Store::class, AssignedUser::class, Product::class,
AssignedProduct::class],
    version = 1, exportSchema = false)
abstract class UniDatabase: RoomDatabase() {

    abstract val userDao: UserDao

    companion object {

        private val businesses = arrayOf(
            Business(1, "Walmart", "King St",
            "Melbourne", "Victoria", "0493959766", "Walmart@gmail.com",
            3096),
            Business(2, "7-eleven", "Malcolm St",
                "Melbourne", "Victoria", "0493959556", "seveneleven@gmail.com",
                3093)
        )

        private val stores = arrayOf(
            Store(1, 1, "Chapel St", "Melbourne",
            "Victoria", "0495673253", 3183, 3000F),
            Store(2, 1, "Greeves St", "Melbourne",
                "Victoria", "0498745535", 3182, 3000F)
        )

        private val users = arrayOf(
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
                "0498629806", "12345678", 'E')
        )

        private val deliveryUser = DeliveryUser(1, "Saddam", "Hussein",
        "saddam@gmail.com", "12345678", "0492121396")

        private val assignUsers = arrayOf(
            AssignedUser(1, 2, 1, 1, "18/07/2020", "13:00"),
            AssignedUser(2, 3, 1, 1, "18/07/2020", "13:00"),
            AssignedUser(3, 4, 1, 1, "18/07/2020", "13:00")
        )

        private val products = arrayOf(
            Product(1, 1, "Coca-Cola", "bottle", "6 pack", 30, "1:6", 1.5F, 0),
            Product(2, 1, "Snickers", "bar", "box", 23, "1:16", 1F, 5),
            Product(3, 1, "Bundaberg Ginger Beer", "bottle", "4 pack", 15, "1:4", 2.6F, 0),
            Product(4, 1, "Schweppes Lemon", "bottle", "4 pack", 20, "1:4", 2F, 10),
            Product(5, 1, "Kirks Ginger Beer", "bottle", "10 pack", 25, "1:10", 1.85F, 0),
            Product(6, 1, "Pepsi Max", "can", "10 pack", 40, "1:10", 1F, 0),
            Product(7, 1, "Tim Tam", "pack", "family box", 50, "1:20", 2.8F, 10),
            Product(8, 1, "Doritos Supreme", "pack", "box", 25, "1:6", 3.2F, 0)
        )

        private val assignProducts = arrayOf(
            AssignedProduct(1, 1, 1, "18/07/2020", "13:00"),
            AssignedProduct(2, 2, 1, "18/07/2020", "13:00"),
            AssignedProduct(3, 3, 1, "18/07/2020", "13:00"),
            AssignedProduct(4, 4, 1, "18/07/2020", "13:00"),
            AssignedProduct(5, 5, 1, "18/07/2020", "13:00"),
            AssignedProduct(6, 6, 2, "18/07/2020", "13:00"),
            AssignedProduct(7, 7, 2, "18/07/2020", "13:00"),
            AssignedProduct(8, 8, 2, "18/07/2020", "13:00")
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

        fun populateDb(userDao: UserDao) {
            //userDao.clear()
            for (i in businesses) {
                userDao.insertBusiness(i)
            }

            for (i in stores) {
                userDao.insertStore(i)
            }

            userDao.insertDeliveryUser(deliveryUser)

            for (i in users) {
                userDao.insertUser(i)
            }

            for(i in assignUsers) {
                userDao.assignUser(i)
            }

            for (i in products) {
                userDao.insertProduct(i)
            }

            for (i in assignProducts) {
                userDao.assignProduct(i)
            }
        }
    }

}