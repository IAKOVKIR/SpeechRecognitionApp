package com.example.audiochatbot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, DeliveryUser::class/*, Business::class, Store::class, AssignedStore::class*/],
    version = 1, exportSchema = false)
abstract class UniDatabase: RoomDatabase() {

    abstract val userDao: UserDao
    abstract val deliveryUserDao: DeliveryUserDao

    companion object {

        private val business = Business(1, "Walmart", "King St",
            "Melbourne", "Victoria", "0493959766", "Walmart@gmail.com",
            3096)
        private val store = Store(1, 1, "Chapel St", "Melbourne",
            "Victoria", "0495673253", 3183, 3000, 0)

        val users = arrayOf(
            User(1, "Jay", "Calingacion", "jay@gmail.com",
                "0498629801", "12345678", 'A'),
            User(2, "Jamie", "Simon", "jamie@gmail.com",
                "0498629802", "12345678", 'E'),
            User(3, "Kirill", "Iakovlev", "kirill@gmail.com",
                "0498629803", "12345678", 'E')
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
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDb(database.userDao)
                    }
                }
            }
        }

        fun populateDb(userDao: UserDao) {
            //database.insertBusiness(business)
            //database.insertStore(store)
            /*repeat(3) {
                database.insertAssignedStore(
                    AssignedStore(it + 1, it + 1, 1, 1,
                    "19/06/2020", "11:10")
                )
            }*/
            userDao.clear()
            for (i in 0..2) {
                userDao.insertUser(users[i])
            }
        }
    }

}