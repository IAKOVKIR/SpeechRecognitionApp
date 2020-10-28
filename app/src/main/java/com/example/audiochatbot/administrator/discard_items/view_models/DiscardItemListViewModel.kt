package com.example.audiochatbot.administrator.discard_items.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class DiscardItemListViewModel(val storeId: Int, val database: UserDao) : ViewModel() {

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
     * a [DiscardItemListViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val discardedItems = database.getDiscardedItemsWithStoreId(storeId)

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _navigateToDiscardItems = MutableLiveData<Boolean>()
    val navigateToDiscardItems get() = _navigateToDiscardItems

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    fun convertStringToAction(text: String) {
        uiScope.launch {
            Log.e("heh", text)
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("discard items") || text.contains("discard an item") || text.contains("discard item"))
                _navigateToDiscardItems.value = true
            else
                _message.value = "Cannot understand your command"
        }
    }

    fun onStoreNavigated() {
        _message.value = null
        _closeFragment.value = null
        _navigateToDiscardItems.value = null
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