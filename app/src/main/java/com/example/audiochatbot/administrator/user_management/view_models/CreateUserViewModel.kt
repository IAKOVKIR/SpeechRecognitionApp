package com.example.audiochatbot.administrator.user_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class CreateUserViewModel(
    private val database: UserDao
) : ViewModel() {
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

    fun submitUser(user: User, adminId: Int) {
        uiScope.launch {
            val uLast = getLastUser()
            user.userId = uLast!!.userId + 1
            user.businessId = getAdminBusinessId(adminId)
            user.position = position
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

    private suspend fun getAdminBusinessId(adminId: Int): Int {
        return withContext(Dispatchers.IO) {
            database.getAdminsBusinessId(adminId)
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