package com.example.audiochatbot.administrator.delivery_list.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreManagementViewModel
import com.example.audiochatbot.database.Delivery
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class DeliveryListViewModel(val businessId: Int,val database: UserDao) : ViewModel() {

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

    val deliveries = database.getAllDeliveries(businessId)

    private val _navigateToDeliveryDetails = MutableLiveData<Int>()
    val navigateToDeliveryDetails
        get() = _navigateToDeliveryDetails

    fun cancelDelivery(delivery: Delivery) {
        uiScope.launch {
            delivery.status = "Canceled"
            updateDelivery(delivery)
        }
    }

    private suspend fun updateDelivery(delivery: Delivery) {
        withContext(Dispatchers.IO) {
            database.updateDelivery(delivery)
        }
    }

    fun onDeliveryClicked(id: Int) {
        _navigateToDeliveryDetails.value = id
    }

    fun onStoreNavigated() {
        _navigateToDeliveryDetails.value = null
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