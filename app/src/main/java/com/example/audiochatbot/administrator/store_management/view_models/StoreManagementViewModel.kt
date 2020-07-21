package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.daos.UserDao

/**
 * ViewModel for StoreManagementFragment.
 */
class StoreManagementViewModel(database: UserDao) : ViewModel() {

    val stores = database.getAllAdminStores()

    private val _navigateToStoreDetails = MutableLiveData<Int>()
    val navigateToStoreDetails
        get() = _navigateToStoreDetails

    fun onStoreClicked(id: Int) {
        _navigateToStoreDetails.value = id
    }

    fun onStoreNavigated() {
        _navigateToStoreDetails.value = null
    }
}