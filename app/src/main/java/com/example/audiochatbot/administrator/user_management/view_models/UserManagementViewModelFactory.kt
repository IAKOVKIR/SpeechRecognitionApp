package com.example.audiochatbot.administrator.user_management.view_models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.daos.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the Application and the UserDao to the ViewModel.
 */
class UserManagementViewModelFactory(
    private val dataSource: UserDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
            return UserManagementViewModel(
                dataSource,
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}