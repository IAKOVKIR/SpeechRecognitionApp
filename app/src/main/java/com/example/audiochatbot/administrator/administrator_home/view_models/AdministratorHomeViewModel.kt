package com.example.audiochatbot.administrator.administrator_home.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreDetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AdministratorHomeViewModel: ViewModel() {
    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()
    private val inventoryCountArray = arrayOf("open inventory count", "inventory count")
    private val deliveryListArray = arrayOf("open delivery list", "delivery list")
    private val discardItemsArray = arrayOf("discard items", "discard item")
    private val cashReportArray = arrayOf("cash report", "open cash report")
    private val userManagementArray = arrayOf("open user management", "user management")
    private val storeManagementArray = arrayOf("open store management", "store management")
    private val productManagementArray = arrayOf("open product management", "product management")
    private val logOutArray = arrayOf("log out")

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

    private var _action = MutableLiveData<Int>()
    val action: LiveData<Int> get() = _action

    fun convertStringToAction(text: String) {
        uiScope.launch {
            when {
                inventoryCountArray.contains(text) -> _action.value = 1
                deliveryListArray.contains(text) -> _action.value = 2
                discardItemsArray.contains(text) -> _action.value = 3
                cashReportArray.contains(text) -> _action.value = 4
                userManagementArray.contains(text) -> _action.value = 5
                storeManagementArray.contains(text) -> _action.value = 6
                productManagementArray.contains(text) -> _action.value = 7
                logOutArray.contains(text) -> _action.value = 8
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