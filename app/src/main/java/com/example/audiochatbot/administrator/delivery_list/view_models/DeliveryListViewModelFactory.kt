package com.example.audiochatbot.administrator.delivery_list.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class DeliveryListViewModelFactory(private val adminId: Int, private val storeId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryListViewModel::class.java)) {
            return DeliveryListViewModel(adminId, storeId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}