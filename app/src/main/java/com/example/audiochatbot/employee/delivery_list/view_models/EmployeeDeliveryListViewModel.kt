package com.example.audiochatbot.employee.delivery_list.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.Time
import com.example.audiochatbot.database.*
import kotlinx.coroutines.*

class EmployeeDeliveryListViewModel(val userId: Int, val storeId: Int, val database: UserDao) : ViewModel() {

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

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        uiScope.launch {
            val text = givenText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else {
                val match = "open delivery number".toRegex().find(text)
                val matchDeliveryNumber = "delivery number".toRegex().find(text)
                val matchDeliveryIsDelivered = "is delivered".toRegex().find(text)

                val index = match?.range?.last
                val indexDeliveryNumber = matchDeliveryNumber?.range?.last
                val indexDeliveryIsDelivered = matchDeliveryIsDelivered?.range?.first

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
                } else if (indexDeliveryNumber != null && indexDeliveryIsDelivered != null) {
                    if (indexDeliveryNumber < indexDeliveryIsDelivered) {
                        val subStr = text.substring(indexDeliveryNumber, indexDeliveryIsDelivered)
                        val num = textToInteger(subStr, 0)

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
                                        deliveredDelivery(delivery)
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
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    fun deliveredDelivery(delivery: Delivery) {
        uiScope.launch {
            delivery.status = "Delivered"
            updateDelivery(delivery)

            val idList = getDeliveryProducts(delivery.deliveryId)
            for (i in idList)
                acceptItems(i)

            _deliveries.value = getItems()
        }
    }

    private fun acceptItems(deliveryProduct: DeliveryProduct) {
        uiScope.launch {
            val conversion = getConversion(deliveryProduct.assignedProductId)
            var smallQuantity = 0
            var bigQuantity = 0

            for (i in conversion.indices) {
                if (conversion[i] == ':') {
                    smallQuantity = conversion.substring(0, i).toInt()
                    bigQuantity = conversion.substring(i + 1).toInt()
                    break
                }
            }

            val newDeliveryProductStatus = DeliveryProductStatus(deliveryProduct.deliveryProductId, userId, "Delivered", time.getDate(), time.getTime())
            addDProductStatus(newDeliveryProductStatus)

            val assignedProduct = getAssignedProduct(deliveryProduct.assignedProductId)
            assignedProduct!!.quantity = assignedProduct.quantity + ((deliveryProduct.smallUnitQuantity * smallQuantity) + (deliveryProduct.bigUnitQuantity * bigQuantity))
            updateAssignedProduct(assignedProduct)
            _deliveries.value = getItems()
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
            result != "" -> result.toInt()
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> -1
        }
    }

    private suspend fun getItems(): List<Delivery> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveriesWithStore(storeId)
        }
    }

    private suspend fun updateDelivery(delivery: Delivery) {
        withContext(Dispatchers.IO) {
            database.updateDelivery(delivery)
        }
    }

    private suspend fun addDProductStatus(deliveryProductStatus: DeliveryProductStatus) {
        withContext(Dispatchers.IO) {
            database.insertDeliveryProductStatus(deliveryProductStatus)
        }
    }

    private suspend fun getConversion(productId: Int): String {
        return withContext(Dispatchers.IO) {
            database.getProductConversionWithAssignedProductId(productId)
        }
    }

    private suspend fun getDeliveryProducts(deliveryId: Int): List<DeliveryProduct> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveryProducts(deliveryId)
        }
    }

    private suspend fun getAssignedProduct(assignedProductId: Int): AssignedProduct? {
        return withContext(Dispatchers.IO) {
            database.getAssignedProduct(assignedProductId)
        }
    }

    private suspend fun updateAssignedProduct(assignedProduct: AssignedProduct) {
        withContext(Dispatchers.IO) {
            database.updateAssignedProduct(assignedProduct)
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