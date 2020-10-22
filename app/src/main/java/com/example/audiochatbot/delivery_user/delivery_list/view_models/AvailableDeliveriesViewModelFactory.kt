package com.example.audiochatbot.delivery_user.delivery_list.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class AvailableDeliveriesViewModelFactory(private val userId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AvailableDeliveriesViewModel::class.java)) {
            return AvailableDeliveriesViewModel(userId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}