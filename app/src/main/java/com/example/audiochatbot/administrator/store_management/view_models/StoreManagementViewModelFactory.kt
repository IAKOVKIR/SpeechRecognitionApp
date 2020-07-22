package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.daos.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the Application and the StoreDao to the ViewModel.
 */
class StoreManagementViewModelFactory(private val adminId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreManagementViewModel::class.java)) {
            return StoreManagementViewModel(adminId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}