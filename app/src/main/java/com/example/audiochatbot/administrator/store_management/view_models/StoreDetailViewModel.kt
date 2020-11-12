package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.Store
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for StoreDetailFragment.
 *
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - UserDao reference.
 */
class StoreDetailViewModel(private val storeId: Int, private val dataSource: UserDao
) : ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

    /** Coroutine setup variables */

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

    /**
     * Lifecycle-aware observable that stores the Store value
     */
    private var _store = MutableLiveData<Store>()
    val store: LiveData<Store> get() = _store

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private val _action = MutableLiveData<Int>()
    val action get() = _action

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            _store.value = retrieveStore(storeId)
        }
    }

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            when (recordedText.toLowerCase()) {
                "go back", "return back" -> _closeFragment.value = true
                "update details", "update", "update the details" -> _action.value = 1
                "remove store", "remove this store", "remove the store" -> deleteRecord()
                "assigned products", "open assigned products", "open the assigned products", "assigns products", "signed products",
                "assign products" -> _action.value = 2
                "assigned users", "open assigned users", "open the assigned users", "signed users", "assign users" -> _action.value = 3
                else -> _message.value = "I am sorry, I cannot understand your command"
            }
        }
    }

    /**
     * method that assigns the store and business ids and updates the store
     */
    fun updateStore(newStore: Store) {
        newStore.storeId = store.value!!.storeId
        newStore.businessId = store.value!!.businessId
        submitStore(newStore)
    }

    /**
     * method that updates the Store
     */
    private fun submitStore(store: Store) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            updateStoreDb(store)
            val u = retrieveStore(storeId)
            if (u!!.storeId == store.storeId)
                _closeFragment.value = true
            else
                _message.value = "Something went wrong"
        }
    }

    /**
     * method that deletes the Store
     */
    fun deleteRecord() {
        //launch a new coroutine in background and continue
        uiScope.launch {
            deleteRecordDb()
            val u = retrieveStore(storeId)
            if (u == null)
                _closeFragment.value = true
            else
                _message.value = "Something went wrong"
        }
    }

    /**
     * Suspending method that retrieves the Store with store id
     */
    private suspend fun retrieveStore(storeKey: Int): Store? {
        return withContext(Dispatchers.IO) {
            database.getStoreWithId(storeKey)
        }
    }

    /**
     * Suspending method that updates the Store record
     */
    private suspend fun updateStoreDb(store: Store) {
        withContext(Dispatchers.IO) {
            database.updateStore(store)
        }
    }

    /**
     * Suspending method that deletes the Store record
     */
    private suspend fun deleteRecordDb() {
        withContext(Dispatchers.IO) {
            database.deleteStoreRecord(storeId)
        }
    }

    /**
     * method that sets a value of null for all LiveData values except for the Store LiveData
     */
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