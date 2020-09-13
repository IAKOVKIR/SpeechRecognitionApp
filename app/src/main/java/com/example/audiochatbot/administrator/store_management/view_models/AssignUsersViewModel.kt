package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.AssignedUser
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class AssignUsersViewModel(val storeId: Int, val businessId: Int, private val database: UserDao): ViewModel() {

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
     * a [AssignUsersViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val users = database.getNotAssignedUsers(storeId, businessId, 'A')

    private val _navigateToUserDetails = MutableLiveData<Int>()
    val navigateToUserDetails
        get() = _navigateToUserDetails

    fun onUserClicked(id: Int) {
        _navigateToUserDetails.value = id
    }

    fun onUserNavigated() {
        _navigateToUserDetails.value = null
    }

    fun addRecord(userId: Int, adminId: Int) {
        uiScope.launch {
            addRecordDb(userId, adminId)
        }
    }

    private suspend fun addRecordDb(userId: Int, adminId: Int) {
        withContext(Dispatchers.IO) {
            val num = database.getLastAssignedUserId() + 1
            database.assignUser(AssignedUser(num, userId, adminId, storeId, "30/07/2020", "12:40"))
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