package com.example.audiochatbot.delivery_user.delivery_list.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class DeliveryUserListViewModelFactory(private val userId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryUserListViewModel::class.java)) {
            return DeliveryUserListViewModel(userId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}