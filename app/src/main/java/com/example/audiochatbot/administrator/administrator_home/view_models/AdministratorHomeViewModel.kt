package com.example.audiochatbot.administrator.administrator_home.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AdministratorHomeViewModel: ViewModel() {
    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()
    private val inventoryCountArray = arrayOf("open the inventory count", "inventory count", "open inventor account", "inventor account")
    private val deliveryListArray = arrayOf("open the delivery list", "delivery list")
    private val discardItemsArray = arrayOf("discard items", "discard item")
    private val cashReportArray = arrayOf("cash report", "open the cash report")
    private val userManagementArray = arrayOf("open the user management", "user management")
    private val storeManagementArray = arrayOf("open the store management", "store management")
    private val productManagementArray = arrayOf("open the product management", "product management")
    private val logOutArray = arrayOf("log out", "logout")

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [AdministratorHomeViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _action = MutableLiveData<Int>()
    val action: LiveData<Int> get() = _action

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(text: String) {
        uiScope.launch {
            val newText = text.toLowerCase()
            when {
                inventoryCountArray.contains(newText) -> _action.value = 1
                deliveryListArray.contains(newText) -> _action.value = 2
                discardItemsArray.contains(newText) -> _action.value = 3
                cashReportArray.contains(newText) -> _action.value = 4
                userManagementArray.contains(newText) -> _action.value = 5
                storeManagementArray.contains(newText) -> _action.value = 6
                productManagementArray.contains(newText) -> _action.value = 7
                logOutArray.contains(newText) -> _action.value = 8
                else -> _action.value = 0
            }
        }
    }

    fun setAction(num: Int) {
        _action.value = num
    }

    fun cancelAction() {
        _action.value = -1
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