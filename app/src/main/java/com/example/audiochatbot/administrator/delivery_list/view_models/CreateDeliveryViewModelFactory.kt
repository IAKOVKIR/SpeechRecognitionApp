package com.example.audiochatbot.administrator.delivery_list.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the StoreID and the UserDao to the ViewModel.
 */
class CreateDeliveryViewModelFactory(private val storeId: Int, private val adminId: Int,
                                       private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateDeliveryViewModel::class.java)) {
            return CreateDeliveryViewModel(storeId, adminId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}