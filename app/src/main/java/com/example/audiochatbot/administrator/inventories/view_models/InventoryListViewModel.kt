package com.example.audiochatbot.administrator.inventories.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.database.models.InventoryCount
import com.example.audiochatbot.database.models.User
import kotlinx.coroutines.*

/**
 * ViewModel for InventoryListFragment.
 *
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - UserDao reference.
 */
class InventoryListViewModel(val storeId: Int, val dataSource: UserDao) : ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

    /** Coroutine setup variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [InventoryListViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of InventoryCount
     */
    val inventories = database.getAllInventoryCountsWithStore(storeId)

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _navigateToInventoryCount = MutableLiveData<Boolean>()
    val navigateToInventoryCount get() = _navigateToInventoryCount

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    /**
     * Lifecycle-aware observable that stores the List of String
     */
    private var _reportList = MutableLiveData<List<String>>()
    val reportList: LiveData<List<String>> get() = _reportList

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(newText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = newText.toLowerCase()
            when {
                text.contains("go back") || text.contains("return back") -> _closeFragment.value =
                    true
                text.contains("inventory count") || text.contains("inventor account") -> _navigateToInventoryCount.value =
                    true
                else -> {
                    val match = "download report number".toRegex().find(text)
                    val match1 = "download a report number".toRegex().find(text)
                    val match2 = "download the report number".toRegex().find(text)

                    val index = match?.range?.last
                    val index1 = match1?.range?.last
                    val index2 = match2?.range?.last

                    val indexNum = index ?: (index1 ?: index2)

                    if (indexNum != null) {

                        val str = text.substring(indexNum + 1)
                        val result = str.filter { it.isDigit() }

                        val num = when {
                            result != "" -> result.toInt()
                            str.contains("one") -> 1
                            str.contains("to") || str.contains("two") -> 2
                            str.contains("three") -> 3
                            str.contains("for") -> 4
                            else -> -1
                        }

                        if (num > 0) {
                            val list = inventories.value
                            var res: InventoryCount? = null

                            if (list != null) {
                                for (i in list) {
                                    if (i.inventoryCountId == num) {
                                        res = i
                                        break
                                    }
                                }

                                if (res != null)
                                    generateAReport(res)
                                else
                                    _message.value = "You do not have an access to this store"
                            } else
                                _message.value = "Cannot understand your command"
                        } else
                            _message.value = "Cannot understand your command"
                    } else
                        _message.value = "Cannot understand your command"
                }
            }
        }
    }

    /**
     * method that generates a report of the InventoryCount
     */
    fun generateAReport(inventoryCount: InventoryCount) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val list = mutableListOf<String>()
            val user = getUser(inventoryCount.userId)

            list.add("Inventory Count ${inventoryCount.inventoryCountId} Report")
            list.add("Store: ${inventoryCount.storeId}")
            list.add("Counted by: ${user.firstName} ${user.lastName} {id : ${user.userId}}")
            list.add("Date of count: ${inventoryCount.date} | ${inventoryCount.time}")
            list.add("Expected count: A$${inventoryCount.expectedEarnings}")
            list.add("Actual count: A$${inventoryCount.totalEarnings}")
            list.add("Difference: A$${inventoryCount.expectedEarnings - inventoryCount.totalEarnings}")

            _reportList.value = list
        }
    }

    /**
     * method that sets all the LiveData to null except for inventories list
     */
    fun onInventoryCountNavigated() {
        _message.value = null
        _closeFragment.value = null
        _navigateToInventoryCount.value = null
    }

    /**
     * Suspending method that retrieves the User with user Id
     */
    private suspend fun getUser(userId: Int): User {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userId)
        }
    }

    /**
     * Called when the ViewModel is dismantled.
     * At this point, we want to cancel all coroutines;
     * otherwise we end up with processes that have nowhere to return to
     * using memory and resources.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}