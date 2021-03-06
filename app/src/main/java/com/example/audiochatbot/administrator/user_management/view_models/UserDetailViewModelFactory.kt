package com.example.audiochatbot.administrator.user_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the User ID and the UserDao to the ViewModel.
 */
class UserDetailViewModelFactory(
    private val userId: Int,
    private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDetailViewModel::class.java)) {
            return UserDetailViewModel(userId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
