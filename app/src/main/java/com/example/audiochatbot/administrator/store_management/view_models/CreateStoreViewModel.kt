package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.Store
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for CreateStoreFragment.
 *
 * @param dataSource - UserDao reference.
 */
class CreateStoreViewModel(private val dataSource: UserDao
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
     * a [CreateStoreViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            when (recordedText.toLowerCase()) {
                "go back", "return back" -> _closeFragment.value = true
                "submit details", "submit", "submit the details" -> _action.value = 1
                else -> _message.value = "I am sorry, I cannot understand your command"
            }
        }
    }

    /**
     * method that assigns store and business ids to the Store and creates it
     */
    fun submitStore(store: Store) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val uLast = getLastStore()
            store.storeId = uLast!!.storeId + 1
            store.businessId = 1
            addStoreToDb(store)
            val u = getLastStore()
            if (u != null) {
                _closeFragment.value = true
            } else {
                _message.value = "Something went wrong"
            }
        }
    }

    /**
     * Suspending method that creates a new Store record
     */
    private suspend fun addStoreToDb(store: Store) {
        withContext(Dispatchers.IO) {
            database.insertStore(store)
        }
    }

    /**
     * Suspending method that retrieves the last Store record
     */
    private suspend fun getLastStore(): Store? {
        return withContext(Dispatchers.IO) {
            database.getLastStore()
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