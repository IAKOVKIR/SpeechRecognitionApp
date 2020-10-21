package com.example.audiochatbot.employee.delivery_list.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class EmployeeDeliveryListViewModelFactory(private val userId: Int, private val storeId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeDeliveryListViewModel::class.java)) {
            return EmployeeDeliveryListViewModel(userId, storeId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}