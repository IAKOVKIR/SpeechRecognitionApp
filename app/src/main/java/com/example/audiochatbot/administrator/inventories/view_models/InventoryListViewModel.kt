package com.example.audiochatbot.administrator.inventories.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class InventoryListViewModel(val storeId: Int, val database: UserDao) : ViewModel() {

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
     * a [InventoryListViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val inventories = database.getAllInventoryCountsWithStore(storeId)

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _navigateToInventoryCount = MutableLiveData<Boolean>()
    val navigateToInventoryCount get() = _navigateToInventoryCount

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    fun convertStringToAction(text: String) {
        uiScope.launch {
            Log.e("heh", text)
            when {
                text.contains("go back") -> _closeFragment.value = true
                text.contains("inventory count") -> _navigateToInventoryCount.value = true
                else -> _message.value = "I'm sorry, I cannot understand your command"
            }
        }
    }

    fun onInventoryCountNavigated() {
        _message.value = null
        _closeFragment.value = null
        _navigateToInventoryCount.value = null
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