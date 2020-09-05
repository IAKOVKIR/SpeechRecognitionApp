package com.example.audiochatbot.administrator.delivery_list.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao

class DeliveryListViewModel(val businessId: Int,val database: UserDao) : ViewModel() {

    val deliveries = database.getAllDeliveries(businessId)

    private val _navigateToDeliveryDetails = MutableLiveData<Int>()
    val navigateToDeliveryDetails
        get() = _navigateToDeliveryDetails

    fun onDeliveryClicked(id: Int) {
        _navigateToDeliveryDetails.value = id
    }

    fun onStoreNavigated() {
        _navigateToDeliveryDetails.value = null
    }
}