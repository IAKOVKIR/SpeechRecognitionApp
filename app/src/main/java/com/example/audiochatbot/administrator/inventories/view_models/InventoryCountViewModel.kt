package com.example.audiochatbot.administrator.inventories.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.*
import kotlinx.coroutines.*

class InventoryCountViewModel(val adminId: Int, val storeId: Int, private val database: UserDao): ViewModel() {

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
     * a [InventoryListViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private var _l = MutableLiveData<List<Int>>()
    val l: LiveData<List<Int>> get() = _l

    private var productObjects: MutableList<Product> = arrayListOf()
    private var smallBigQuantities: MutableList<Int> = arrayListOf()

    init {
        uiScope.launch {
            _products.value = getItems()
            for (element in products.value!!) {
                productObjects.add(element)
            }

            smallBigQuantities = List(productObjects.size * 2) { 0 } as MutableList<Int>
        }
    }

    private val _isDone = MutableLiveData<Boolean>()
    val isDone
        get() = _isDone

    fun addItem(productId: Int, smallQuantity: Int, bigQuantity: Int) {
        uiScope.launch {
            if (smallQuantity != 0 || bigQuantity != 0) {
                for (i in 0 until productObjects.size) {
                    if (productId == productObjects[i].productId) {
                        smallBigQuantities[i * 2] = smallQuantity
                        smallBigQuantities[i * 2 + 1] = bigQuantity
                    }
                }
            }

            _l.value = smallBigQuantities.toList()
            _products.value = getItems()

            Log.e("size", "${productObjects.size}")
        }
    }

    fun removeItem(productId: Int) {
        uiScope.launch {
            var num = 0
            for (i in 0 until productObjects.size) {
                if (productId == productObjects[i].productId) {
                    num = i
                    break
                }
            }

            smallBigQuantities[num * 2] = 0
            smallBigQuantities[num * 2 + 1] = 0

            _l.value = smallBigQuantities.toList()
            _products.value = getItems()

            Log.e("size", "${productObjects.size}")
        }
    }

    fun submitInventoryCount(currentEarnings: Float) {
        uiScope.launch {
            val list = getAssignedItems()
            var totalPrice = 0F

            for (element in list) {
                for (j in 0 until productObjects.size) {
                    if (element.productId == productObjects[j].productId && (smallBigQuantities[j * 2] != 0 || smallBigQuantities[j * 2 + 1] != 0)) {
                        val con = productObjects[j].conversion
                        var smallQuantity = 0
                        var bigQuantity = 0

                        for (i in con.indices) {
                            if (con[i] == ':') {
                                smallQuantity = con.substring(0, i).toInt()
                                bigQuantity = con.substring(i + 1).toInt()
                                break
                            }
                        }

                        val newQuantity = smallQuantity * smallBigQuantities[j * 2] + bigQuantity * smallBigQuantities[j * 2 + 1]
                        val difference = element.quantity - newQuantity
                        if (difference > 0) {
                            element.quantity = newQuantity
                            totalPrice += productObjects[j].price * difference * (1F - (element.sale / 100F))
                        } else {
                            Log.e("seriouesly", "less")
                        }
                        break
                    }
                }
            }
            if (totalPrice > 0) {
                for (element in list) {
                    updateAssignedItem(element)
                }

                val lastId = getLastId()
                finishCount(InventoryCount(lastId + 1, storeId, adminId, totalPrice, currentEarnings, "20/09/2020", "14:00"))

                _isDone.value = true
            }
        }
    }

    private suspend fun updateAssignedItem(assignedProduct: AssignedProduct) {
        withContext(Dispatchers.IO) {
            database.updateAssignedProduct(assignedProduct)
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

    private suspend fun finishCount(inventoryCount: InventoryCount) {
        withContext(Dispatchers.IO) {
            database.insertInventoryCount(inventoryCount)
        }
    }

    private suspend fun getLastId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastInventoryCountId()
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