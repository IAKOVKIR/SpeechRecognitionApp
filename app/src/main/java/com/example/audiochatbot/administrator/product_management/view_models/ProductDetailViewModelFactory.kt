package com.example.audiochatbot.administrator.product_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the key for the product and the UserDao to the ViewModel.
 */
class ProductDetailViewModelFactory(
    private val productKey: Int,
    private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            return ProductDetailViewModel(productKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}