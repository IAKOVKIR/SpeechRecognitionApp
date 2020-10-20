package com.example.audiochatbot.employee.cash_report.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class SelectStoreViewModelFactory(private val userId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectStoreViewModel::class.java)) {
            return SelectStoreViewModel(userId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}