package com.example.audiochatbot.delivery_user.delivery_list.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.Time
import com.example.audiochatbot.database.models.Delivery
import com.example.audiochatbot.database.models.DeliveryProductStatus
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for DeliveryUserListFragment.
 *
 * @param userId - the key of the current user we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - the reference to UniDatabase
 */
class DeliveryUserListViewModel(val userId: Int, val storeId: Int, val dataSource: UserDao) : ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

    private val time = Time()

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
     * a [DeliveryUserListViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of Deliveries
     */
    private var _deliveries = MutableLiveData<List<Delivery>>()
    val deliveries: LiveData<List<Delivery>> get() = _deliveries

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Delivery value
     */
    private val _navigateToDeliveryDetails = MutableLiveData<Delivery>()
    val navigateToDeliveryDetails
        get() = _navigateToDeliveryDetails

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _navigateToCreateNewDelivery = MutableLiveData<Boolean>()
    val navigateToCreateNewDelivery get() = _navigateToCreateNewDelivery

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        refreshTheList()
    }

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(newText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = newText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("add new delivery") || text.contains("create new delivery") || text.contains("add a new delivery") || text.contains("add a delivery")
                || text.contains("add delivery") || text.contains("create a delivery") || text.contains("create delivery") || text.contains("create a new delivery"))
                _navigateToCreateNewDelivery.value = true
            else {
                val pattern = "open delivery number".toRegex()
                val patternCancelDelivery = "cancel delivery number".toRegex()

                val match = pattern.find(text)
                val matchCancelDelivery = patternCancelDelivery.find(text)

                val index = match?.range?.last
                val indexCancelDelivery = matchCancelDelivery?.range?.last

                if (index != null) {
                    val num = textToInteger(text, index)
                    var del: Delivery? = null

                    if (num > 0) {
                        val list = deliveries.value

                        if (list != null) {
                            for (i in list) {
                                if (i.deliveryId == num) {
                                    del = i
                                    break
                                }
                            }

                            if (del != null)
                                _navigateToDeliveryDetails.value = del
                            else
                                _message.value = "You do not have an access to this delivery"
                        } else
                            _message.value = "The store does not have any deliveries"
                    } else
                        _message.value = "Cannot understand your command"
                } else if (indexCancelDelivery != null) {
                    val num = textToInteger(text, indexCancelDelivery)

                    if (num > 0) {
                        val list = deliveries.value
                        var delivery: Delivery? = null

                        if (list != null) {
                            for (i in list) {
                                if (i.deliveryId == num) {
                                    delivery = i
                                    break
                                }
                            }

                            if (delivery != null)
                                if (delivery.status == "In Transit")
                                    cancelDelivery(delivery)
                                else
                                    _message.value = "The delivery is already canceled or delivered"
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

    /**
     * method that cancels the delivery
     */
    fun cancelDelivery(delivery: Delivery) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            delivery.status = "Canceled"
            updateDelivery(delivery)

            val idList = getIDs(delivery.deliveryId)
            declineItems(idList[0])

            _deliveries.value = getItems()
        }
    }

    /**
     * method that declines the item
     */
    private fun declineItems(deliveryProductId: Int) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val newDeliveryProductStatus = DeliveryProductStatus(deliveryProductId, userId, "Canceled", time.getDate(), time.getTime())
            addDProductStatus(newDeliveryProductStatus)
        }
    }

    /**
     * method that refreshes the list of deliveries
     */
    fun refreshTheList() {
        //launch a new coroutine in background and continue
        uiScope.launch {
            _deliveries.value = getItems()
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
            str.contains("for") || str.contains("four") -> 4
            else -> -1
        }
    }

    /**
     * Suspending method that updates the delivery
     */
    private suspend fun updateDelivery(delivery: Delivery) {
        withContext(Dispatchers.IO) {
            database.updateDelivery(delivery)
        }
    }

    /**
     * Suspending method that retrieves the list of deliveries
     */
    private suspend fun getItems(): List<Delivery> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveriesWithDeliveryUserAndStoreID(userId, storeId)
        }
    }

    /**
     * Suspending method that retrieves delivery product id
     */
    private suspend fun getIDs(deliveryId: Int): List<Int> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveryProductIDs(deliveryId)
        }
    }

    /**
     * Suspending method that add a delivery product status record
     */
    private suspend fun addDProductStatus(deliveryProductStatus: DeliveryProductStatus) {
        withContext(Dispatchers.IO) {
            database.insertDeliveryProductStatus(deliveryProductStatus)
        }
    }

    /**
     * method that sets a value of clicked delivery
     */
    fun onDeliveryClicked(delivery: Delivery) {
        _navigateToDeliveryDetails.value = delivery
    }

    /**
     * method that sets a value of null for all LiveData values except for the list
     */
    fun onStoreNavigated() {
        _navigateToDeliveryDetails.value = null
        _message.value = null
        _closeFragment.value = null
        _navigateToCreateNewDelivery.value = null
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