package com.example.audiochatbot.administrator.discard_items.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides User, Store IDs and the UserDao to the ViewModel.
 */
class DiscardItemViewModelFactory(private val adminId: Int, private val storeId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscardItemViewModel::class.java)) {
            return DiscardItemViewModel(adminId, storeId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}