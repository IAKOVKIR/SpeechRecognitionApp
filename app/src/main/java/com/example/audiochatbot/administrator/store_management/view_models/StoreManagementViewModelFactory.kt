package com.example.audiochatbot.administrator.store_management.view_models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.daos.StoreDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the Application and the StoreDao to the ViewModel.
 */
class StoreManagementViewModelFactory(
    private val dataSource: StoreDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreManagementViewModel::class.java)) {
            return StoreManagementViewModel(
                dataSource,
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}