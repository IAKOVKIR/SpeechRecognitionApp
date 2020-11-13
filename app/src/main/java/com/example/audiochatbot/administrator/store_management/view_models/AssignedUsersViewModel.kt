package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for AssignedUsersFragment.
 *
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - UserDao reference.
 */
class AssignedUsersViewModel(val storeId: Int, private val dataSource: UserDao): ViewModel() {

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
     * a [AssignedUsersViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of Users
     */
    val users = database.getAllUsersLiveWithStoreIDNoAdmins(storeId)

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private val _navigateToUserDetails = MutableLiveData<Int>()
    val navigateToUserDetails
        get() = _navigateToUserDetails

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _navigateToAssignUsers = MutableLiveData<Boolean>()
    val navigateToAssignUsers
        get() = _navigateToAssignUsers

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

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
            else if (text.contains("assign users") || text.contains("assign user") || text.contains("assign the user")
                || text.contains("assign a user") || text.contains("sign users") || text.contains("assigns users")
                || text.contains("signed users"))
                _navigateToAssignUsers.value = true
            else {
                val matchAddProductNumber = "remove user number".toRegex().find(text)
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
                                deleteRecord(num)
                            else
                                _message.value = "You do not have an access to this user"
                        } else
                            _message.value = "User list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    /**
     * method that converts text to number
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
     * method that sets a value of clicked user
     */
    fun onUserClicked(id: Int) {
        _navigateToUserDetails.value = id
    }

    /**
     * method that sets a value of null for all LiveData values except for the list
     */
    fun onUserNavigated() {
        _navigateToUserDetails.value = null
        _navigateToAssignUsers.value = null
        _message.value = null
        _closeFragment.value = null
    }

    /**
     * method that deletes the record
     */
    fun deleteRecord(userId: Int) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            deleteRecordDb(userId)
            val deletedProducts = getRecordDb(userId)
            if (deletedProducts == 0)
                _message.value = "The user was removed successfully "
            else
                _message.value = "Something went wrong"
        }
    }

    /**
     * Suspending method that removes the AssignedUser record
     */
    private suspend fun deleteRecordDb(userId: Int) {
        withContext(Dispatchers.IO) {
            database.removeUserFromStore(userId, storeId)
        }
    }

    /**
     * Suspending method that retrieves the AssignedUser id
     */
    private suspend fun getRecordDb(productId: Int) : Int {
        return withContext(Dispatchers.IO) {
            database.ifUserAssigned(productId, storeId)
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