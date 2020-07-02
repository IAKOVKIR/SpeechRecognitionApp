package com.example.audiochatbot.administrator.user_management

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class CreateUserViewModel(
    val database: UserDao,
    application: Application) : AndroidViewModel(application) {
    private val positionCharArray = arrayOf('E', 'A', 'D')

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
     * a [CreateUserViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var position = positionCharArray[0]

    private val _isUploaded = MutableLiveData<Boolean>()
    val isUploaded
        get() = _isUploaded

    fun setPos(pos: Int) {
        position = positionCharArray[pos]
    }

    fun addUser(user: User) {
        user.position = position
        submitUser(user)
    }

    private fun submitUser(user: User) {
        uiScope.launch {
            addUserToDb(user)
            val u = getLastUser()
            _isUploaded.value = u != null
        }
    }

    private suspend fun addUserToDb(user: User) {
        withContext(Dispatchers.IO) {
            database.insertUser(user)

        }
    }

    private suspend fun getLastUser(): User? {
        return withContext(Dispatchers.IO) {
            database.getLastUser()
        }
    }
}