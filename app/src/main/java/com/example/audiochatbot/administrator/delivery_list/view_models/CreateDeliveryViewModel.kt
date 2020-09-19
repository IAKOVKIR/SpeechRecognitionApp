package com.example.audiochatbot.administrator.delivery_list.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.*
import kotlinx.coroutines.*

class CreateDeliveryViewModel(val storeId: Int, private val database: UserDao): ViewModel() {

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
     * a [CreateDeliveryViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private var _l = MutableLiveData<List<Int>>()
    val l: LiveData<List<Int>> get() = _l

    private var productIds: MutableList<Int> = arrayListOf()
    private var smallBigQuantities: MutableList<Int> = arrayListOf()

    init {
        uiScope.launch {
            _products.value = getItems()
            for (element in products.value!!) {
                productIds.add(element.productId)
            }

            smallBigQuantities = List(productIds.size * 2) { 0 } as MutableList<Int>
        }
    }

    private val _isDone = MutableLiveData<Boolean>()
    val isDone
        get() = _isDone

    fun addItem(productId: Int, smallQuantity: Int, bigQuantity: Int) {
        uiScope.launch {
            if (smallQuantity != 0 || bigQuantity != 0) {
                for (i in 0 until productIds.size) {
                    if (productId == productIds[i]) {
                        smallBigQuantities[i * 2] = smallQuantity
                        smallBigQuantities[i * 2 + 1] = bigQuantity
                    }
                }
            }

            _l.value = smallBigQuantities.toList()
            _products.value = getItems()

            Log.e("size", "${productIds.size}")
        }
    }

    fun removeItem(productId: Int) {
        uiScope.launch {
            val num = productIds.indexOf(productId)

            productIds.removeAt(num)
            smallBigQuantities.removeAt(num * 2)
            smallBigQuantities.removeAt(num * 2)

            Log.e("size", "${productIds.size}")
        }
    }

    fun submitDelivery() {
        uiScope.launch {
            val deliveryId = getLastDeliveryId() + 1
            val delivery = Delivery(deliveryId, storeId, "Waiting", "18/07/2020", "13:00")
            addNewDelivery(delivery)

            val itemList: MutableList<DeliveryProduct> = arrayListOf()
            val list = getAssignedItems()

            for (element in list) {
                for (j in 0 until productIds.size) {
                    if (element.productId == productIds[j]) {
                        itemList.add(DeliveryProduct(deliveryId, productIds[j], smallBigQuantities[j * 2], smallBigQuantities[j * 2 - 1], "not available"))
                        break
                    }
                }
            }

            val newList = itemList.toList()
            addNewItems(newList)

            val checkId = getLastDeliveryId()
            if (checkId == deliveryId) {
                _isDone.value = true
            }
        }
    }

    private suspend fun getLastDeliveryId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastDeliveryId()
        }
    }

    private suspend fun addNewDelivery(delivery: Delivery) {
        withContext(Dispatchers.IO) {
            database.insertDelivery(delivery)
        }
    }

    private suspend fun addNewItems(itemList: List<DeliveryProduct>) {
        withContext(Dispatchers.IO) {
            database.insertDeliveryProducts(itemList)
        }
    }

    private suspend fun getAssignedItems(): List<AssignedProduct> {
        return withContext(Dispatchers.IO) {
            database.getAssignedProductsList(storeId)
        }
    }

    private suspend fun getItems(): List<Product> {
        return withContext(Dispatchers.IO) {
            database.getAllProductsWithStoreID(storeId)
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