package com.example.audiochatbot.administrator.delivery_list.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class DeliveryDetailsViewModelFactory(private val userId: Int,private val deliveryId: Int,
                                      private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryDetailsViewModel::class.java)) {
            return DeliveryDetailsViewModel(userId, deliveryId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}