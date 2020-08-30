package com.example.audiochatbot.administrator.discard_items.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao

class DiscardItemStoreViewModel(val adminId: Int,val database: UserDao) : ViewModel() {

    val stores = database.getAllAdminStores(adminId)

    private val _navigateToDiscardItem = MutableLiveData<Int>()
    val navigateToDiscardItem
        get() = _navigateToDiscardItem

    fun onStoreClicked(id: Int) {
        _navigateToDiscardItem.value = id
    }

    fun onStoreNavigated() {
        _navigateToDiscardItem.value = null
    }
}