package com.example.audiochatbot.administrator.discard_items.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

class DiscardItemStoreViewModelFactory(private val adminId: Int, private val dataSource: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscardItemStoreViewModel::class.java)) {
            return DiscardItemStoreViewModel(adminId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}