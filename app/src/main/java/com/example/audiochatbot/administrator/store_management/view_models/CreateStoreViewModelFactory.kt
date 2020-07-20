package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.daos.StoreDao

class CreateStoreViewModelFactory(private val adminId: Int,
    private val dataSource: StoreDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateStoreViewModel::class.java)) {
            return CreateStoreViewModel(adminId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}