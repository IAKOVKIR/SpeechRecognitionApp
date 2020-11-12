package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel for StoreManagementFragment.
 *
 * @param adminId - the key of the current admin user we are working on.
 * @param dataSource - UserDao reference.
 */
class StoreManagementViewModel(val adminId: Int,val dataSource: UserDao) : ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

    /** Coroutine setup variables */

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
     * a [StoreManagementViewModel] update the UI after performing some processing.
     */

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of Store
     */
    val stores = database.getAllAdminStores(adminId)

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private val _navigateToStoreDetails = MutableLiveData<Int>()
    val navigateToStoreDetails
        get() = _navigateToStoreDetails

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _navigateToCreateNewStore = MutableLiveData<Boolean>()
    val navigateToCreateNewUser get() = _navigateToCreateNewStore

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = recordedText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("add new store") || text.contains("create new store") || text.contains("add a new store")
                || text.contains("create a new store")) {
                _navigateToCreateNewStore.value = true
            }  else {
                val matchOpenStoreNumber = "open store number".toRegex().find(text)
                val matchOpenStoreNumber1 = "open storm number".toRegex().find(text)
                val indexOpenProductNumber = matchOpenStoreNumber?.range?.last
                val indexOpenProductNumber1 = matchOpenStoreNumber1?.range?.last

                if (indexOpenProductNumber != null || indexOpenProductNumber1 != null) {
                    val num = when {
                        indexOpenProductNumber != null -> {
                            textToInteger(text, indexOpenProductNumber)
                        }
                        else -> textToInteger(text, indexOpenProductNumber1!!)
                    }

                    if (num > 0) {
                        val list = stores.value
                        var res = false

                        if (list != null) {
                            for (i in list) {
                                if (i.storeId == num) {
                                    res = true
                                    break
                                }
                            }

                            if (res)
                                _navigateToStoreDetails.value = num
                            else
                                _message.value = "You do not have an access to this user"
                        } else
                            _message.value = "User list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    /**
     * method that converts text to number
     */
    private fun textToInteger(text: String, lastIndex: Int): Int {
        val str = text.substring(lastIndex + 1)
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> result.toInt()
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> -1
        }
    }

    /**
     * method that sets a value of clicked Store
     */
    fun onStoreClicked(id: Int) {
        _navigateToStoreDetails.value = id
    }

    /**
     * method that sets a value of null for all LiveData values except for the list of Store
     */
    fun onStoreNavigated() {
        _navigateToStoreDetails.value = null
        _navigateToCreateNewStore.value = null
        _message.value = null
        _closeFragment.value = null
    }

    /**
     * Called when the ViewModel is dismantled.
     * At this point, we want to cancel all coroutines;
     * otherwise we end up with processes that have nowhere to return to
     * using memory and resources.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}