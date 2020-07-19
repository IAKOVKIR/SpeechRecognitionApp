package com.example.audiochatbot.administrator.user_management.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModel
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.daos.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for SleepQualityFragment.
 *
 * @param userKey The key of the current user we are working on.
 */
class UserDetailViewModel(
    private val userKey: Int,
    dataSource: UserDao
) : ViewModel() {
    private val positionCharArray = arrayOf('E', 'A', 'D')

    /**
     * Hold a reference to SleepDatabase via its SleepDatabaseDao.
     */
    val database = dataSource

    /** Coroutine setup variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = Job()

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [CreateUserViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private var position = 'E'

    private val _isUploaded = MutableLiveData<Boolean>()
    val isUploaded
        get() = _isUploaded

    init {
        getUserScope()
    }

    fun setPos(pos: Int) {
        position = positionCharArray[pos]
    }

    fun updateUser(newUser: User) {
        newUser.position = position
        newUser.userId = user.value!!.userId
        submitUser(newUser)
    }

    private fun submitUser(user: User) {
        uiScope.launch {
            addUserToDb(user)
            val u = getUpdatedUser(user.userId)
            _isUploaded.value = u!!.userId == user.userId
        }
    }

    private fun getUserScope() {
        uiScope.launch {
            _user.value = retrieveUser(userKey)
        }
    }

    private suspend fun retrieveUser(userKey: Int): User? {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userKey)
        }
    }

    private suspend fun addUserToDb(user: User) {
        withContext(Dispatchers.IO) {
            database.update(user)
        }
    }

    private suspend fun getUpdatedUser(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userId)
        }
    }

    /**
     * Cancels all coroutines when the ViewModel is cleared, to cleanup any pending work.
     *
     * onCleared() gets called when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

