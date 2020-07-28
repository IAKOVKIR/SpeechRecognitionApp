package com.example.audiochatbot.administrator.product_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the Business ID and the UserDao to the ViewModel.
 */
class ProductManagementViewModelFactory(private val businessId: Int,
                                     private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductManagementViewModel::class.java)) {
            return ProductManagementViewModel(businessId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}