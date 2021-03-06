package com.example.audiochatbot.administrator.discard_items.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel for DiscardItemStoreFragment.
 *
 * @param adminId - the key of the current user we are working on.
 * @param dataSource - UserDao reference.
 */
class DiscardItemStoreViewModel(val adminId: Int,val dataSource: UserDao) : ViewModel() {

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
     * a [DiscardItemStoreViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of Store
     */
    val stores = database.getAllAdminStores(adminId)

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private val _navigateToDiscardItem = MutableLiveData<Int>()
    val navigateToDiscardItem
        get() = _navigateToDiscardItem

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(newText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = newText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else {
                val pattern = "store number".toRegex()
                val match = pattern.find(text)
                val index = match?.range?.last

                if (index != null) {
                    val str = text.substring(index + 1)
                    val result = str.filter { it.isDigit() }

                    val num = when {
                        result != "" -> result.toInt()
                        str.contains("one") -> 1
                        str.contains("to") || str.contains("two") -> 2
                        str.contains("three") -> 3
                        str.contains("for") -> 4
                        else -> -1
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

                            if (res)
                                _navigateToDiscardItem.value = num
                            else
                                _message.value = "You do not have an access to this store"
                        } else
                            _message.value = "Cannot understand your command"
                    } else
                        _message.value = "Cannot understand your command"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    /**
     * method that updates the LiveData by assigning the id of the selected Store
     */
    fun onStoreClicked(id: Int) {
        _navigateToDiscardItem.value = id
    }

    /**
     * method that sets the values of LiveData to null except for the Store List
     */
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