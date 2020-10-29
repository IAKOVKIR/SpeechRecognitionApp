package com.example.audiochatbot.administrator.delivery_list.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.AssignedProduct
import com.example.audiochatbot.database.DeliveryProduct
import com.example.audiochatbot.database.DeliveryProductStatus
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class DeliveryDetailsViewModel(val userId: Int, val deliveryId: Int, private val database: UserDao): ViewModel() {

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
            else
                _message.value = "Cannot recognise your command"
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

            val newDeliveryProductStatus = DeliveryProductStatus(deliveryProduct.deliveryProductId, userId, "accepted", "13/07/2020", "13:00")
            addDProductStatus(newDeliveryProductStatus)

            val assignedProduct = getAssignedProduct(deliveryProduct.assignedProductId)
            assignedProduct!!.quantity = assignedProduct.quantity + ((deliveryProduct.smallUnitQuantity * smallQuantity) + (deliveryProduct.bigUnitQuantity * bigQuantity))
            updateAssignedProduct(assignedProduct)
            _deliveryProducts.value = getItems()
        }
    }

    fun declineItems(deliveryProduct: DeliveryProduct) {
        uiScope.launch {
            val newDeliveryProductStatus = DeliveryProductStatus(deliveryProduct.deliveryProductId, userId, "declined", "13/07/2020", "13:00")
            addDProductStatus(newDeliveryProductStatus)
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

    private suspend fun getDProductStatus(deliveryProductId: Int): DeliveryProductStatus? {
        return withContext(Dispatchers.IO) {
            database.getDeliveryProductStatus(deliveryProductId)
        }
    }

    private suspend fun addDProductStatus(deliveryProductStatus: DeliveryProductStatus) {
        withContext(Dispatchers.IO) {
            database.insertDeliveryProductStatus(deliveryProductStatus)
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