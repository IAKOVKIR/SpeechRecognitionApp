package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the StoreID and the UserDao to the ViewModel.
 */
class AssignedUsersViewModelFactory(private val storeId: Int,
    private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssignedUsersViewModel::class.java)) {
            return AssignedUsersViewModel(storeId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}