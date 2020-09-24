package com.example.audiochatbot.administrator.cash_report.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreDetailViewModel
import com.example.audiochatbot.database.CashOperation
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for CashReportFragment.
 */
class CashReportViewModel(val adminId: Int, val storeId: Int,val database: UserDao) : ViewModel() {
    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [CashReportViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _store = MutableLiveData<Store>()
    val store: LiveData<Store> get() = _store

    val cashReports = database.getAllCashReports(storeId)

    init {
        uiScope.launch {
            _store.value = retrieveStore(storeId)
        }
    }

    fun depositOrWithdrawMoney(amount: Float, operation: Boolean) {
        uiScope.launch {
            if (amount > 0) {
                val id = getCashReportId() + 1
                if (operation) {
                    val newCashReport = CashOperation(id, adminId, storeId, amount, true, "20/07/2020", "14:00")
                    uploadCashReport(newCashReport)

                    _store.value!!.cashOnHand = store.value!!.cashOnHand + amount
                    updateStore(store.value!!)
                    _store.value = retrieveStore(storeId)
                } else {
                    if (amount <= store.value!!.cashOnHand) {
                        val newCashReport = CashOperation(id, adminId, storeId, amount, false, "20/07/2020", "14:00")
                        uploadCashReport(newCashReport)

                        _store.value!!.cashOnHand = store.value!!.cashOnHand - amount
                        updateStore(store.value!!)
                        _store.value = retrieveStore(storeId)
                    }
                }
            }
        }
    }

    private suspend fun retrieveStore(storeKey: Int): Store? {
        return withContext(Dispatchers.IO) {
            database.getStoreWithId(storeKey)
        }
    }

    private suspend fun uploadCashReport(cashOperation: CashOperation) {
        withContext(Dispatchers.IO) {
            database.insertCashOperation(cashOperation)
        }
    }

    private suspend fun getCashReportId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastCashReportId()
        }
    }

    private suspend fun updateStore(newStore: Store) {
        withContext(Dispatchers.IO) {
            database.updateStore(newStore)
        }
    }

    /**
     * Cancels all coroutines when the ViewModel is cleared, to cleanup any pending work.
     *
     * onCleared() gets called when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}