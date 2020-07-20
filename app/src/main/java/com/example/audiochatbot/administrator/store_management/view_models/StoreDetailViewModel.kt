package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModel
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.database.daos.StoreDao
import kotlinx.coroutines.*

class StoreDetailViewModel(private val storeId: Int, private val database: StoreDao
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
     * a [CreateUserViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _store = MutableLiveData<Store>()
    val store: LiveData<Store> get() = _store

    private val _isDone = MutableLiveData<Boolean>()
    val isDone
        get() = _isDone

    init {
        getStoreScope()
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
            _isDone.value = u!!.storeId == store.storeId
        }
    }

    private fun getStoreScope() {
        uiScope.launch {
            _store.value = retrieveStore(storeId)
        }
    }

    fun deleteRecord() {
        uiScope.launch {
            deleteRecordDb()
            val u = retrieveStore(storeId)
            _isDone.value = u == null
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
            database.deleteRecord(storeId)
        }
    }

}