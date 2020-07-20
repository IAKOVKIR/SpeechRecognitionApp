package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.daos.StoreDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * ViewModel for StoreManagementFragment.
 */
class StoreManagementViewModel(val database: StoreDao) : ViewModel() {

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

    val stores = database.getAllStores()

    private val _navigateToStoreDetails = MutableLiveData<Int>()
    val navigateToStoreDetails
        get() = _navigateToStoreDetails

    fun onStoreClicked(id: Int) {
        _navigateToStoreDetails.value = id
    }

    fun onStoreNavigated() {
        _navigateToStoreDetails.value = null
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