package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.Store
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class CreateStoreViewModel(private val database: UserDao
) : ViewModel() {

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
     * a [CreateStoreViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    private val _isUploaded = MutableLiveData<Boolean>()
    val isUploaded
        get() = _isUploaded

    fun submitStore(store: Store, adminId: Int) {
        uiScope.launch {
            val uLast = getLastStore()
            store.storeId = uLast!!.storeId + 1
            store.businessId = getBusinessId(adminId)
            addStoreToDb(store)
            val u = getLastStore()
            _isUploaded.value = u != null
        }
    }

    private suspend fun addStoreToDb(store: Store) {
        withContext(Dispatchers.IO) {
            database.insertStore(store)
        }
    }

    private suspend fun getLastStore(): Store? {
        return withContext(Dispatchers.IO) {
            database.getLastStore()
        }
    }

    private suspend fun getBusinessId(adminId: Int): Int {
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