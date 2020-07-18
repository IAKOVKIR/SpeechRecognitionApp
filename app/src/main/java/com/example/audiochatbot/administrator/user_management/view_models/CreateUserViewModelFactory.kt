package com.example.audiochatbot.administrator.user_management.view_models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.daos.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the application and the UserDao to the ViewModel.
 */
class CreateUserViewModelFactory(
    private val dataSource: UserDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateUserViewModel::class.java)) {
            return CreateUserViewModel(
                dataSource,
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}