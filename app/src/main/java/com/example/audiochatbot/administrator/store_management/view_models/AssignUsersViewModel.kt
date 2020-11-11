package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.Time
import com.example.audiochatbot.database.models.AssignedUser
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for AssignUsersFragment.
 *
 * @param adminId - the key of the current admin user we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param businessId - the key of the current business we are working on.
 * @param dataSource - UserDao reference.
 */
class AssignUsersViewModel(private val adminId: Int, val storeId: Int, val businessId: Int, private val dataSource: UserDao): ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

    /** Coroutine setup variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()

    private val time = Time()

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [AssignUsersViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of AssignedUsers
     */
    val users = database.getNotAssignedUsers(storeId, businessId, 'A')

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private val _navigateToUserDetails = MutableLiveData<Int>()
    val navigateToUserDetails
        get() = _navigateToUserDetails

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = recordedText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else {
                val matchOpenProductNumber = "open user number".toRegex().find(text)
                val matchAddProductNumber = "add user number".toRegex().find(text)

                val indexOpenProductNumber = matchOpenProductNumber?.range?.last
                val indexAddProductNumber = matchAddProductNumber?.range?.last

                if (indexAddProductNumber != null) {
                    val num = textToInteger(text, indexAddProductNumber)

                    if (num > 0) {
                        val list = users.value
                        var res = false

                        if (list != null) {
                            for (i in list) {
                                if (i.userId == num) {
                                    res = true
                                    break
                                }
                            }

                            if (res)
                                addRecord(num)
                            else
                                _message.value = "You do not have an access to this product"
                        } else
                            _message.value = "Product list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else if (indexOpenProductNumber != null) {
                    val num = textToInteger(text, indexOpenProductNumber)

                    if (num > 0) {
                        val list = users.value
                        var res = false

                        if (list != null) {
                            for (i in list) {
                                if (i.userId == num) {
                                    res = true
                                    break
                                }
                            }

                            if (res)
                                _navigateToUserDetails.value = num
                            else
                                _message.value = "You do not have an access to this product"
                        } else
                            _message.value = "Product list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    /**
     * method that converts the text to number
     */
    private fun textToInteger(text: String, lastIndex: Int): Int {
        val str = text.substring(lastIndex + 1)
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> result.toInt()
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> -1
        }
    }

    /**
     * method that sets a value of clicked User
     */
    fun onUserClicked(id: Int) {
        _navigateToUserDetails.value = id
    }

    /**
     * method that sets a value of null for _navigateToUserDetails LiveData
     */
    fun onUserNavigated() {
        _navigateToUserDetails.value = null
    }

    /**
     * method that assigns the User
     */
    fun addRecord(userId: Int) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            addRecordDb(userId)
        }
    }

    /**
     * Suspending method that inserts the AssignedUser record
     */
    private suspend fun addRecordDb(userId: Int) {
        withContext(Dispatchers.IO) {
            val num = database.getLastAssignedUserId() + 1
            database.assignUser(AssignedUser(num, userId, adminId, storeId, time.getDate(), time.getTime()))
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