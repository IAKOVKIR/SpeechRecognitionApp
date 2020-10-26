package com.example.audiochatbot.employee.delivery_list.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.Delivery
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class EmployeeDeliveryListViewModel(val userId: Int, val storeId: Int, val database: UserDao) : ViewModel() {

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
     * a [EmployeeDeliveryListViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _deliveries = MutableLiveData<List<Delivery>>()
    val deliveries: LiveData<List<Delivery>> get() = _deliveries

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _navigateToDeliveryDetails = MutableLiveData<Int>()
    val navigateToDeliveryDetails
        get() = _navigateToDeliveryDetails

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        uiScope.launch {
            _deliveries.value = getItems()
        }
    }

    fun convertStringToAction(text: String) {
        uiScope.launch {
            Log.e("heh", text)
            if (text.contains("go back"))
                _closeFragment.value = true
            else {
                val match = "open delivery number".toRegex().find(text)

                val index = match?.range?.last

                if (index != null) {
                    val num = textToInteger(text, index)

                    if (num > 0) {
                        val list = deliveries.value
                        var res = false

                        if (list != null) {
                            for (i in list) {
                                if (i.deliveryId == num) {
                                    res = true
                                    break
                                }
                            }

                            if (res)
                                _navigateToDeliveryDetails.value = num
                            else
                                _message.value = "You do not have an access to this delivery"
                        } else
                            _message.value = "The store does not have any deliveries"
                    } else
                        _message.value = "Cannot understand your command"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    fun refreshTheList() {
        uiScope.launch {
            _deliveries.value = getItems()
        }
    }

    private fun textToInteger(text: String, lastIndex: Int): Int {
        val str = text.substring(lastIndex + 1)
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> {
                Log.e("heh", result)
                result.toInt()
            }
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("for") -> 4
            else -> -1
        }
    }

    private suspend fun getItems(): List<Delivery> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveriesWithStoreIDAndStatus(storeId, "Delivered")
        }
    }

    fun onDeliveryClicked(id: Int) {
        _navigateToDeliveryDetails.value = id
    }

    fun onStoreNavigated() {
        _navigateToDeliveryDetails.value = null
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