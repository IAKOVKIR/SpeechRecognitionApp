package com.example.audiochatbot.administrator.discard_items.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DiscardItemStoreViewModel(val adminId: Int,val database: UserDao) : ViewModel() {
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
     * a [DiscardItemStoreViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val stores = database.getAllAdminStores(adminId)

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _navigateToDiscardItem = MutableLiveData<Int>()
    val navigateToDiscardItem
        get() = _navigateToDiscardItem

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    fun convertStringToAction(text: String) {
        uiScope.launch {

            if (text.contains("go back"))
                _closeFragment.value = true
            else {
                val pattern = "store number".toRegex()
                val match = pattern.find(text)
                val index = match?.range?.last
                var num = 0

                if (index != null) {
                    val str = text.substring(index)
                    val result = str.filter { it.isDigit() }

                    when {
                        result != "" -> {
                            Log.e("heh", result)
                            num = result.toInt()
                        }
                        text.contains("store number one") -> num = 1
                        text.contains("store number to") -> num = 2
                        text.contains("store number for") -> num = 4
                    }
                }

                if (num > 0) {
                    val list = stores.value
                    var res = false

                    if (list != null) {
                        for (i in list) {
                            if (i.storeId == num) {
                                res = true
                                break
                            }
                        }
                    }

                    if (res)
                        _navigateToDiscardItem.value = num
                    else
                        _message.value = "You do not have an access to this store"
                } else
                    _message.value = "Can't understand your command"
            }
        }
    }

    fun onStoreClicked(id: Int) {
        _navigateToDiscardItem.value = id
    }

    fun onStoreNavigated() {
        _navigateToDiscardItem.value = null
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