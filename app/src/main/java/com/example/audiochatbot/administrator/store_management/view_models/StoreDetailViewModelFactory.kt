package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides Store ID and the UserDao to the ViewModel.
 */
class StoreDetailViewModelFactory(private val storeId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreDetailViewModel::class.java)) {
            return StoreDetailViewModel(storeId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}