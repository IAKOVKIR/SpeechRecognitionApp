package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.daos.StoreDao

class StoreDetailViewModelFactory(private val storeId: Int, private val dataSource: StoreDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreDetailViewModel::class.java)) {
            return StoreDetailViewModel(storeId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}