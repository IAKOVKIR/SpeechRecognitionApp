package com.example.audiochatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.DeliveryUserDao
import com.example.audiochatbot.database.UserDao

class LoginViewModelFactory(private val userDataSource: UserDao,
                            private val deliveryUserDataSource: DeliveryUserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userDataSource, deliveryUserDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}