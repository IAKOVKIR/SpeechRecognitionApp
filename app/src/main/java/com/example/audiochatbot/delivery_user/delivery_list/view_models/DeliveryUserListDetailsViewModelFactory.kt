package com.example.audiochatbot.delivery_user.delivery_list.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.administrator.delivery_list.view_models.CreateDeliveryViewModel
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the StoreID and the UserDao to the ViewModel.
 */
class DeliveryUserListDetailsViewModelFactory(private val storeId: Int, private val adminId: Int,
                                     private val deliveryId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryUserListDetailsViewModel::class.java)) {
            return DeliveryUserListDetailsViewModel(storeId, adminId, deliveryId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}