package com.example.audiochatbot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.audiochatbot.database.daos.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, DeliveryUser::class, Business::class, Store::class, AssignedUser::class],
    version = 1, exportSchema = false)
abstract class UniDatabase: RoomDatabase() {

    abstract val userDao: UserDao

    companion object {

        private val business = Business(1, "Walmart", "King St",
            "Melbourne", "Victoria", "0493959766", "Walmart@gmail.com",
            3096)
        private val store = Store(1, 1, "Chapel St", "Melbourne",
            "Victoria", "0495673253", 3183, 3000F)

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
                "0498629805", "12345678", 'E')
        )

        private val deliveryUser = DeliveryUser(1, "Saddam", "Hussein",
        "saddam@gmail.com", "12345678", "0492121396")

        private val assignStores = arrayOf(
            AssignedUser(1, 2, 1, 1, "18/07/2020", "13:00")
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
            userDao.insertBusiness(business)
            userDao.insertStore(store)

            userDao.insertDeliveryUser(deliveryUser)
            for (i in users) {
                userDao.insertUser(i)
            }

            for(i in assignStores) {
                userDao.assignUser(i)
            }
        }
    }

}