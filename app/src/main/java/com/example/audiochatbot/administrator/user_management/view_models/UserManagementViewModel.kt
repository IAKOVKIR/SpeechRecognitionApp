package com.example.audiochatbot.administrator.user_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for UserManagementFragment.
 *
 * @param businessId - the key of the current business we are working on.
 * @param dataSource - UserDao reference.
 */
class UserManagementViewModel(private val businessId: Int, val dataSource: UserDao) : ViewModel() {

    /**
     * Hold a reference to SleepDatabase via its SleepDatabaseDao.
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
     * a [UserManagementViewModel] update the UI after performing some processing.
     */

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the list of Product
     */
    private var _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

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
    private val _navigateToCreateNewUser = MutableLiveData<Boolean>()
    val navigateToCreateNewUser get() = _navigateToCreateNewUser

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            _users.value = getAllUsers(businessId)
        }
    }

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
            else if (text.contains("add new user") || text.contains("create new user") || text.contains("create a new user")
                || text.contains("add a new user") || text.contains("create a user") || text.contains("add a user")) {
                _navigateToCreateNewUser.value = true
            } else {
                val patternOpenProductNumber = "open user number".toRegex()
                val patternOpenProduct = "open".toRegex()
                val patternFindProduct = "find".toRegex()

                val matchOpenProductNumber = patternOpenProductNumber.find(text)
                val matchOpenProduct = patternOpenProduct.find(text)
                val matchFindProduct = patternFindProduct.find(text)

                val indexOpenProductNumber = matchOpenProductNumber?.range?.last
                val indexOpenProduct = matchOpenProduct?.range?.last
                val indexFindProduct = matchFindProduct?.range?.last

                if (indexOpenProductNumber != null) {
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
                                _message.value = "You do not have an access to this user"
                        } else
                            _message.value = "User list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else if (indexOpenProduct != null) {
                    val str = text.substring(indexOpenProduct + 1)
                    val list = users.value
                    var num = -1

                    if (list != null) {
                        for (i in list) {
                            if (str.contains("${i.firstName.toLowerCase()} ${i.lastName.toLowerCase()}")) {
                                num = i.userId
                                break
                            }
                        }

                        if (num != -1)
                            _navigateToUserDetails.value = num
                        else
                            _message.value = "You do not have an access to this user"
                    } else
                        _message.value = "User list is empty"
                } else if (indexFindProduct != null) {
                    val str = recordedText.substring(indexFindProduct + 1)
                    retrieveList(str)
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    /**
     * method that refreshes the list of users
     */
    fun retrieveList(str: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            if (str.trim() != "")
                _users.value = getListWithString("%$str%", businessId)
            else
                _users.value = getAllUsers(businessId)
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
        _navigateToCreateNewUser.value = null
        _message.value = null
        _closeFragment.value = null
    }

    /**
     * Suspending method that retrieves the list of the users
     */
    private suspend fun getAllUsers(businessId: Int): List<User> {
        return withContext(Dispatchers.IO) {
            database.getAllUsersWithBusinessId(businessId)
        }
    }

    /**
     * Suspending method that retrieves the list of the users
     */
    private suspend fun getListWithString(line: String, businessId: Int): List<User> {
        return withContext(Dispatchers.IO) {
            database.getAllUsersWithString(line, businessId)
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