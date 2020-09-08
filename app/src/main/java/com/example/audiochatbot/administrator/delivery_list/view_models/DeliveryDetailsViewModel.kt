package com.example.audiochatbot.administrator.delivery_list.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreManagementViewModel
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
     * a [StoreManagementViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val deliveryProducts = database.getAllDeliveryProducts(deliveryId)

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