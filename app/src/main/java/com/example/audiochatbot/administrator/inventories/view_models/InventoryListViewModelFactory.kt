package com.example.audiochatbot.administrator.inventories.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class InventoryListViewModelFactory(private val businessId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryListViewModel::class.java)) {
            return InventoryListViewModel(businessId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}