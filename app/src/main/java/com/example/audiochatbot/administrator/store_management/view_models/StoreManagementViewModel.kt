package com.example.audiochatbot.administrator.store_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao

/**
 * ViewModel for StoreManagementFragment.
 */
class StoreManagementViewModel(val adminId: Int,val database: UserDao) : ViewModel() {

    val stores = database.getAllAdminStores(adminId)

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