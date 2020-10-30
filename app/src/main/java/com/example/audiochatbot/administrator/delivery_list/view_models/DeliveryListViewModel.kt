package com.example.audiochatbot.administrator.delivery_list.view_models

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.*
import kotlinx.coroutines.*

class DeliveryListViewModel(val adminId: Int, val storeId: Int, val database: UserDao) : ViewModel() {

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
     * a [DeliveryListViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _deliveries = MutableLiveData<List<Delivery>>()
    val deliveries: LiveData<List<Delivery>> get() = _deliveries

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _navigateToDeliveryDetails = MutableLiveData<Delivery>()
    val navigateToDeliveryDetails
        get() = _navigateToDeliveryDetails

    private val _navigateToCreateNewDelivery = MutableLiveData<Boolean>()
    val navigateToCreateNewDelivery get() = _navigateToCreateNewDelivery

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
            else if (text.contains("add new delivery") || text.contains("create new delivery") ||
                text.contains("add delivery") || text.contains("create delivery"))
                _navigateToCreateNewDelivery.value = true
            else {
                val pattern = "open delivery number".toRegex()
                val patternCancelDelivery = "cancel delivery number".toRegex()
                val patternDeliveryNumber = "delivery number".toRegex()
                val patternDeliveryIsDelivered = "is delivered".toRegex()

                val match = pattern.find(text)
                val matchCancelDelivery = patternCancelDelivery.find(text)
                val matchDeliveryNumber = patternDeliveryNumber.find(text)
                val matchDeliveryIsDelivered = patternDeliveryIsDelivered.find(text)

                val index = match?.range?.last
                val indexCancelDelivery = matchCancelDelivery?.range?.last
                val indexDeliveryNumber = matchDeliveryNumber?.range?.last
                val indexDeliveryIsDelivered = matchDeliveryIsDelivered?.range?.first

                if (index != null) {
                    val num = textToInteger(text, index)

                    if (num > 0) {
                        val list = deliveries.value
                        var del: Delivery? = null

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
                                if (delivery.status != "Canceled" || delivery.status != "Delivered")
                                    updateDeliveryStatus(delivery, "Canceled")
                                else
                                    _message.value = "The delivery is already cancelled or delivered"
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

    private fun deliveredDelivery(delivery: Delivery) {
        uiScope.launch {
            delivery.status = "Delivered"
            updateDelivery(delivery)

            val idList = getDeliveryProducts(delivery.deliveryId)
            for (i in idList) {
                acceptItems(i)
            }

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

            Log.d("s / b", "$smallQuantity / $bigQuantity")

            val newDeliveryProductStatus = DeliveryProductStatus(deliveryProduct.deliveryProductId, adminId, "Delivered", "13/07/2020", "13:00")
            addDProductStatus(newDeliveryProductStatus)

            val assignedProduct = getAssignedProduct(deliveryProduct.assignedProductId)
            assignedProduct!!.quantity = assignedProduct.quantity + ((deliveryProduct.smallUnitQuantity * smallQuantity) + (deliveryProduct.bigUnitQuantity * bigQuantity))
            updateAssignedProduct(assignedProduct)
            _deliveries.value = getItems()
        }
    }

    fun updateDeliveryStatus(delivery: Delivery, status: String) {
        uiScope.launch {
            delivery.status = status
            updateDelivery(delivery)
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
            result != "" -> {
                Log.e("heh", result)
                result.toInt()
            }
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> -1
        }
    }

    private suspend fun updateDelivery(delivery: Delivery) {
        withContext(Dispatchers.IO) {
            database.updateDelivery(delivery)
        }
    }

    private suspend fun getItems(): List<Delivery> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveriesWithStore(storeId)
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

    fun onDeliveryClicked(delivery: Delivery) {
        _navigateToDeliveryDetails.value = delivery
    }

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