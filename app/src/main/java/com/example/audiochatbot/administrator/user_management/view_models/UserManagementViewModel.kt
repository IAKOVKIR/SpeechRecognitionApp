package com.example.audiochatbot.administrator.user_management.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for UserManagementFragment.
 */
class UserManagementViewModel(private val businessId: Int, private val database: UserDao) : ViewModel() {

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

    private var _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _navigateToUserDetails = MutableLiveData<Int>()
    val navigateToUserDetails
        get() = _navigateToUserDetails

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    init {
        uiScope.launch {
            _users.value = getAllUsers(businessId)
        }
    }

    fun retrieveList(str: String) {
        uiScope.launch {
            if (str != "")
                _users.value = getListWithString("%$str%", businessId)
            else
                _users.value = getAllUsers(businessId)
        }
    }

    fun onUserClicked(id: Int) {
        _navigateToUserDetails.value = id
    }


    fun onUserNavigated() {
        _navigateToUserDetails.value = null
    }

    private suspend fun getAllUsers(businessId: Int): List<User> {
        return withContext(Dispatchers.IO) {
            database.getAllUsersWithBusinessId(businessId)
        }
    }

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