package com.example.audiochatbot.administrator.delivery_list.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.AssignedProduct
import com.example.audiochatbot.database.DeliveryProduct
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class DeliveryDetailsViewModel(val deliveryId: Int, private val database: UserDao): ViewModel() {

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
     * a [DeliveryDetailsViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _deliveryProducts = MutableLiveData<List<DeliveryProduct>>()
    val deliveryProducts: LiveData<List<DeliveryProduct>> get() = _deliveryProducts

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        uiScope.launch {
            _deliveryProducts.value = getItems()
        }
    }

    fun convertStringToAction(text: String) {
        uiScope.launch {
            Log.e("heh", text)
            if (text.contains("go back"))
                _closeFragment.value = true
            else {
                val patternAcceptId = "except item set number".toRegex() //google recognizes accept as except
                val patternAcceptId1 = "except itemset number".toRegex() //google recognizes accept as except
                val patternDeclineId = "decline item set number".toRegex()
                val patternDeclineId1 = "decline itemset number".toRegex()

                val matchAcceptId = patternAcceptId.find(text)
                val matchAcceptId1 = patternAcceptId1.find(text)
                val matchDeclineId = patternDeclineId.find(text)
                val matchDeclineId1 = patternDeclineId1.find(text)

                val indexAcceptId = matchAcceptId?.range?.last
                val indexAcceptId1 = matchAcceptId1?.range?.last
                val indexDeclineId = matchDeclineId?.range?.last
                val indexDeclineId1 = matchDeclineId1?.range?.last

                when {
                    indexAcceptId != null || indexAcceptId1 != null-> {
                        val list = deliveryProducts.value?.toList()

                        if (list != null) {
                            Log.e("step 0", "passed")
                            val num = if (indexAcceptId != null) {
                                textToInteger(text, indexAcceptId)
                            } else {
                                textToInteger(text, indexAcceptId1!!)
                            }
                            var obj: DeliveryProduct? = null

                            for (i in list.indices) {
                                if (num == list[i].assignedProductId) {
                                    if (list[i].status == "not available") {
                                        obj = list[i]
                                        break
                                    }
                                }
                            }

                            if (obj != null)
                                acceptItems(obj)
                            else
                                _message.value = "Cannot understand your command"
                        }
                    }
                    indexDeclineId != null || indexDeclineId1 != null -> {
                        val list = deliveryProducts.value?.toList()

                        if (list != null) {
                            Log.e("step 0", "passed")
                            val num = if (indexDeclineId != null) {
                                textToInteger(text, indexDeclineId)
                            } else {
                                textToInteger(text, indexDeclineId1!!)
                            }
                            var obj: DeliveryProduct? = null

                            for (i in list.indices) {
                                if (num == list[i].assignedProductId) {
                                    if (list[i].status == "not available") {
                                        obj = list[i]
                                        break
                                    }
                                }
                            }

                            if (obj != null)
                                declineItems(obj)
                            else
                                _message.value = "Cannot understand your command"
                        }
                    }
                    else -> _message.value = "Cannot recognise your command"
                }
            }
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

    fun acceptItems(deliveryProduct: DeliveryProduct) {
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

            deliveryProduct.status = "accepted"
            updateDeliveryProduct(deliveryProduct)

            val assignedProduct = getAssignedProduct(deliveryProduct.assignedProductId)
            assignedProduct!!.quantity = assignedProduct.quantity + ((deliveryProduct.smallUnitQuantity * smallQuantity) + (deliveryProduct.bigUnitQuantity * bigQuantity))
            updateAssignedProduct(assignedProduct)
            _deliveryProducts.value = getItems()
        }
    }

    fun declineItems(deliveryProduct: DeliveryProduct) {
        uiScope.launch {
            deliveryProduct.status = "declined"
            updateDeliveryProduct(deliveryProduct)
            _deliveryProducts.value = getItems()
        }
    }

    private suspend fun updateDeliveryProduct(deliveryProduct: DeliveryProduct) {
        withContext(Dispatchers.IO) {
            database.updateDeliveryProduct(deliveryProduct)
        }
    }

    private suspend fun updateAssignedProduct(assignedProduct: AssignedProduct) {
        withContext(Dispatchers.IO) {
            database.updateAssignedProduct(assignedProduct)
        }
    }

    private suspend fun getAssignedProduct(assignedProductId: Int): AssignedProduct? {
        return withContext(Dispatchers.IO) {
            database.getAssignedProduct(assignedProductId)
        }
    }

    private suspend fun getConversion(productId: Int): String {
        return withContext(Dispatchers.IO) {
            database.getProductConversionWithAssignedProductId(productId)
        }
    }

    private suspend fun getItems(): List<DeliveryProduct> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveryProducts(deliveryId)
        }
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