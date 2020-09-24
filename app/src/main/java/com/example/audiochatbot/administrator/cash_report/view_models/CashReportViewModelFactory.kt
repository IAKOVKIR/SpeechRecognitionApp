package com.example.audiochatbot.administrator.cash_report.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.database.UserDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides Admin and Store IDs and the UserDao to the ViewModel.
 */
class CashReportViewModelFactory(private val adminId: Int, private val storeId: Int, private val database: UserDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CashReportViewModel::class.java)) {
            return CashReportViewModel(adminId, storeId, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}