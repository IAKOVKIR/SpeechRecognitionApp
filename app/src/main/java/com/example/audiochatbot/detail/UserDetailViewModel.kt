package com.example.audiochatbot.detail

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.Job

/**
 * ViewModel for SleepQualityFragment.
 *
 * @param userKey The key of the current user we are working on.
 */
class UserDetailViewModel(
    private val userKey: Int = 0,
    dataSource: UserDao
) : ViewModel() {

    /**
     * Hold a reference to SleepDatabase via its SleepDatabaseDao.
     */
    val database = dataSource

    /** Coroutine setup variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = Job()

    private val user = MediatorLiveData<User>()

    fun getUser() = user

    init {
        user.addSource(database.getUserWithId(userKey), user::setValue)
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

