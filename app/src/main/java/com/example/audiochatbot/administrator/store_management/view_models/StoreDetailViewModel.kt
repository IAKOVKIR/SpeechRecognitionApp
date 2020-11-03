package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.Store
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class StoreDetailViewModel(private val storeId: Int, private val database: UserDao
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
     * a [StoreDetailViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _store = MutableLiveData<Store>()
    val store: LiveData<Store> get() = _store

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    private val _action = MutableLiveData<Int>()
    val action get() = _action

    init {
        uiScope.launch {
            _store.value = retrieveStore(storeId)
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        uiScope.launch {
            when (recordedText.toLowerCase()) {
                "go back", "return back" -> _closeFragment.value = true
                "update details", "update", "update the details" -> _action.value = 1
                "remove store", "remove this store", "remove the store" -> deleteRecord()
                "assigned products", "open assigned items", "open the assigned items" -> _action.value = 2
                "assigned users", "open assigned users", "open the assigned users" -> _action.value = 3
                else -> _message.value = "I am sorry, I cannot understand your command"
            }
        }
    }

    fun updateStore(newStore: Store) {
        newStore.storeId = store.value!!.storeId
        newStore.businessId = store.value!!.businessId
        submitStore(newStore)
    }

    private fun submitStore(store: Store) {
        uiScope.launch {
            updateStoreDb(store)
            val u = retrieveStore(storeId)
            if (u!!.storeId == store.storeId)
                _closeFragment.value = true
            else
                _message.value = "Something went wrong"
        }
    }

    fun deleteRecord() {
        uiScope.launch {
            deleteRecordDb()
            val u = retrieveStore(storeId)
            if (u == null)
                _closeFragment.value = true
            else
                _message.value = "Something went wrong"
        }
    }

    private suspend fun retrieveStore(storeKey: Int): Store? {
        return withContext(Dispatchers.IO) {
            database.getStoreWithId(storeKey)
        }
    }

    private suspend fun updateStoreDb(store: Store) {
        withContext(Dispatchers.IO) {
            database.updateStore(store)
        }
    }

    private suspend fun deleteRecordDb() {
        withContext(Dispatchers.IO) {
            database.deleteStoreRecord(storeId)
        }
    }

    fun onActionNavigated() {
        _action.value = null
        _message.value = null
        _closeFragment.value = null
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