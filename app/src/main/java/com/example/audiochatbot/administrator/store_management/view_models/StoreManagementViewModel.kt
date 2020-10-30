package com.example.audiochatbot.administrator.store_management.view_models

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
 * ViewModel for StoreManagementFragment.
 */
class StoreManagementViewModel(val adminId: Int,val dataSource: UserDao) : ViewModel() {

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
     * a [StoreManagementViewModel] update the UI after performing some processing.
     */

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val stores = database.getAllAdminStores(adminId)

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _navigateToStoreDetails = MutableLiveData<Int>()
    val navigateToStoreDetails
        get() = _navigateToStoreDetails

    private val _navigateToCreateNewStore = MutableLiveData<Boolean>()
    val navigateToCreateNewUser get() = _navigateToCreateNewStore

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        uiScope.launch {
            val text = recordedText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("add new store") || text.contains("create new store") || text.contains("add a new store")
                || text.contains("create a new store")) {
                _navigateToCreateNewStore.value = true
            }  else {
                val matchOpenStoreNumber = "open store number".toRegex().find(text)
                val indexOpenProductNumber = matchOpenStoreNumber?.range?.last

                if (indexOpenProductNumber != null) {
                    val num = textToInteger(text, indexOpenProductNumber)

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
                                _navigateToStoreDetails.value = num
                            else
                                _message.value = "You do not have an access to this user"
                        } else
                            _message.value = "User list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    private fun textToInteger(text: String, lastIndex: Int): Int {
        val str = text.substring(lastIndex + 1)
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> result.toInt()
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> -1
        }
    }

    fun onStoreClicked(id: Int) {
        _navigateToStoreDetails.value = id
    }

    fun onStoreNavigated() {
        _navigateToStoreDetails.value = null
        _navigateToCreateNewStore.value = null
        _message.value = null
        _closeFragment.value = null
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