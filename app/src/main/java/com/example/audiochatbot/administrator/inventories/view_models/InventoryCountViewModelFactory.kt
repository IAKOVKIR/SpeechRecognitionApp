package com.example.audiochatbot.administrator.inventories.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the StoreID and the UserDao to the ViewModel.
 */
class InventoryCountViewModelFactory(private val adminId: Int, private val storeId: Int,
                                     private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryCountViewModel::class.java)) {
            return InventoryCountViewModel(adminId, storeId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}