package com.example.audiochatbot.employee.cash_report.view_models

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

/**
 * ViewModel for EmployeeCashReportFragment.
 *
 * @param userId - the key of the current user we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - UserDao reference.
 */
class EmployeeCashReportViewModel(val userId: Int, val storeId: Int,val dataSource: UserDao) : ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

    /** Coroutine setup variables */

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
     * a [EmployeeCashReportViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val cashReports = database.getAllEmployeeCashReports(userId, storeId)

    /**
     * Lifecycle-aware observable that stores the Store value
     */
    private var _store = MutableLiveData<Store>()
    val store: LiveData<Store> get() = _store

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            _store.value = retrieveStore(storeId)
        }
    }

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        //launch a new coroutine in background and continue
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

    /**
     * method that makes a deposit or withdrawal
     */
    fun depositOrWithdrawMoney(amount: Float, operation: Boolean) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            if (amount > 0) {
                val id = getCashReportId() + 1
                if (operation) {
                    val newCashReport = CashOperation(id, userId, storeId, amount, true, time.getDate(), time.getTime())
                    uploadCashReport(newCashReport)

                    _store.value!!.cashOnHand = store.value!!.cashOnHand + amount
                    updateStore(store.value!!)
                    _store.value = retrieveStore(storeId)
                } else if (amount <= store.value!!.cashOnHand) {
                    val newCashReport = CashOperation(id, userId, storeId, amount, false, time.getDate(), time.getTime())
                    uploadCashReport(newCashReport)

                    _store.value!!.cashOnHand = store.value!!.cashOnHand - amount
                    updateStore(store.value!!)
                    _store.value = retrieveStore(storeId)
                }
            }
        }
    }

    /**
     * method that rounds to 2 decimal places
     */
    private fun Float.round(decimals: Int): Float {
        var multiplier = 1F
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    /**
     * method that converts text to a number
     */
    private fun convertTextToNumber(text: String): Float {
        return when {
            text.contains("one") -> 1F
            text.contains("to") || text.contains("two") -> 2F
            text.contains("three") -> 3F
            text.contains("for") -> 4F
            else -> -1F
        }
    }

    /**
     * method that retrieves the Store with storeKey
     */
    private suspend fun retrieveStore(storeKey: Int): Store? {
        return withContext(Dispatchers.IO) {
            database.getStoreWithId(storeKey)
        }
    }

    /**
     * method that add a new CashOperation record
     */
    private suspend fun uploadCashReport(cashOperation: CashOperation) {
        withContext(Dispatchers.IO) {
            database.insertCashOperation(cashOperation)
        }
    }

    /**
     * method that retrieves the id of the last Cash Operation
     */
    private suspend fun getCashReportId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastCashReportId()
        }
    }

    /**
     * method that updates the Store object
     */
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