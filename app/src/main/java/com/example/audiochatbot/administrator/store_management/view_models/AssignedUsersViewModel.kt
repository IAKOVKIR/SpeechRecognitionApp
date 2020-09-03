package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class AssignedUsersViewModel(val storeId: Int, private val database: UserDao): ViewModel() {

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
     * a [StoreManagementViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val users = database.getAllUsersLiveWithStoreIDNoAdmins(storeId)

    private val _navigateToUserDetails = MutableLiveData<Int>()
    val navigateToUserDetails
        get() = _navigateToUserDetails

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    fun onUserClicked(id: Int) {
        _navigateToUserDetails.value = id
    }

    fun onUserNavigated() {
        _navigateToUserDetails.value = null
    }

    fun onMessageCleared() {
        _errorMessage.value = null
    }

    fun deleteRecord(userId: Int) {
        uiScope.launch {
            deleteRecordDb(userId)
        }
    }

    private suspend fun deleteRecordDb(userId: Int) {
        withContext(Dispatchers.IO) {
            database.removeUserFromStore(userId, storeId)
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