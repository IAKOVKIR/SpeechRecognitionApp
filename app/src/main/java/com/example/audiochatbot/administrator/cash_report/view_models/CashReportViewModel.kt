package com.example.audiochatbot.administrator.cash_report.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.Time
import com.example.audiochatbot.database.models.CashOperation
import com.example.audiochatbot.database.models.Store
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*
import java.lang.NumberFormatException
import kotlin.math.round

/**
 * ViewModel for CashReportFragment.
 */
class CashReportViewModel(val adminId: Int, val storeId: Int,val database: UserDao) : ViewModel() {
    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()
    private val time = Time()

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

    val cashReports = database.getAllCashReports(storeId)

    private var _store = MutableLiveData<Store>()
    val store: LiveData<Store> get() = _store

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        uiScope.launch {
            _store.value = retrieveStore(storeId)
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        uiScope.launch {
            val text = givenText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else {
                val matchDeposit = "deposit".toRegex().find(text)
                val matchWithdraw = "withdraw".toRegex().find(text)
                val matchDollar = "dollar".toRegex().find(text)

                val indexDeposit = matchDeposit?.range?.last
                val indexWithdraw = matchWithdraw?.range?.last
                val indexDollar = matchDollar?.range?.first

                if (indexDollar != null) {
                    if (indexDeposit != null) {
                        if (indexDeposit < indexDollar) {
                            val str = text.substring(indexDeposit + 1, indexDollar)

                            val num = try {
                                val result = str.toFloat()
                                result.round(2)
                            } catch (e: NumberFormatException) {
                                convertTextToNumber(str)
                            }

                            if (num > 0)
                                depositOrWithdrawMoney(num, true)
                            else if (num == -1F)
                                _message.value = "Can't understand your command"
                        } else
                            _message.value = "Can't understand your command"
                    } else if (indexWithdraw != null) {
                        if (indexWithdraw < indexDollar) {
                            val str = text.substring(indexWithdraw + 1, indexDollar)
                            val num = try {
                                val result = str.toFloat()
                                result.round(2)
                            } catch (e: NumberFormatException) {
                                convertTextToNumber(str)
                            }

                            if (num > 0)
                                depositOrWithdrawMoney(num, false)
                            else if (num == -1F)
                                _message.value = "Can't understand your command"
                        } else
                            _message.value = "Can't understand your command"
                    } else
                        _message.value = "Can't understand your command"
                } else if (indexDeposit != null) {
                    if (indexDeposit < text.length - 3) {
                        if (text[indexDeposit + 2] == '$') {
                            val str = text.substring(indexDeposit + 3)
                            val num = try {
                                val result = str.toFloat()
                                result.round(2)
                            } catch (e: NumberFormatException) {
                                convertTextToNumber(str)
                            }

                            if (num > 0)
                                depositOrWithdrawMoney(num, true)
                            else if (num == -1F)
                                _message.value = "Can't understand your command"
                        } else
                            _message.value = "Can't understand your command"
                    } else
                        _message.value = "Can't understand your command"
                } else if (indexWithdraw != null) {
                    if (indexWithdraw < text.length - 3) {
                        if (text[indexWithdraw + 2] == '$') {
                            val str = text.substring(indexWithdraw + 3)
                            val num = try {
                                val result = str.toFloat()
                                result.round(2)
                            } catch (e: NumberFormatException) {
                                convertTextToNumber(str)
                            }

                            if (num > 0)
                                depositOrWithdrawMoney(num, false)
                            else if (num == -1F)
                                _message.value = "Can't understand your command"
                        } else
                            _message.value = "Can't understand your command"
                    } else
                        _message.value = "Can't understand your command"
                } else
                    _message.value = "Can't understand your command"
            }
        }
    }

    fun depositOrWithdrawMoney(amount: Float, operation: Boolean) {
        uiScope.launch {
            if (amount > 0) {
                val id = getCashReportId() + 1
                if (operation) {
                    val newCashReport = CashOperation(id, adminId, storeId, amount, true, time.getDate(), time.getTime())
                    uploadCashReport(newCashReport)

                    _store.value!!.cashOnHand = store.value!!.cashOnHand + amount
                    updateStore(store.value!!)
                    _store.value = retrieveStore(storeId)
                } else {
                    if (amount <= store.value!!.cashOnHand) {
                        val newCashReport = CashOperation(id, adminId, storeId, amount, false, time.getDate(), time.getTime())
                        uploadCashReport(newCashReport)

                        _store.value!!.cashOnHand = store.value!!.cashOnHand - amount
                        updateStore(store.value!!)
                        _store.value = retrieveStore(storeId)
                    }
                }
            }
        }
    }

    private fun Float.round(decimals: Int): Float {
        var multiplier = 1F
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    private fun convertTextToNumber(text: String): Float {
        return when {
            text.contains("one") -> 1F
            text.contains("to") || text.contains("two") -> 2F
            text.contains("three") -> 3F
            text.contains("for") -> 4F
            else -> -1F
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