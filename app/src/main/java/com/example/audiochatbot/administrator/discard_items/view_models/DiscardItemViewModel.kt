package com.example.audiochatbot.administrator.discard_items.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreManagementViewModel
import com.example.audiochatbot.database.AssignedProduct
import com.example.audiochatbot.database.DiscardedItem
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class DiscardItemViewModel(val storeId: Int, val database: UserDao) : ViewModel() {

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

    val products = database.getAllProductsLiveWithStoreID(storeId)

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    fun discardItem(productId: Int, userId: Int, quantity: Int) {
        uiScope.launch {
            if (quantity > 0) {
                val num = getQuantity(productId)
                if (num >= quantity) {
                    val aId = dItem(productId, userId, quantity)
                    val aItem = getAssignedProduct(aId)
                    aItem!!.quantity -= quantity
                    updateAssignedItem(aItem!!)
                    if (quantity == 1)
                        _message.value = "item is discarded"
                    else
                        _message.value = "Items are discarded"
                } else
                    _message.value = "the entered value is bigger then the quantity of the product"
            } else
                _message.value = "the value of the item is less than 1"
        }
    }

    private suspend fun dItem(productId: Int, userId: Int, quantity: Int): Int {
        return withContext(Dispatchers.IO) {
            val num = database.getLastDiscardedItemId() + 1
            val apId = database.getAssignedProductId(productId, storeId)
            val item = DiscardedItem(num, apId, userId, quantity, "30/07/2020", "12:40")
            database.discardItem(item)
            apId
        }
    }

    private suspend fun getQuantity(productId: Int): Int {
        return withContext(Dispatchers.IO) {
            database.getAssignedProductQuantity(productId, storeId)
        }
    }

    private suspend fun getAssignedProduct(id: Int): AssignedProduct? {
        return withContext(Dispatchers.IO) {
            database.getAssignedProduct(id)
        }
    }

    private suspend fun updateAssignedItem(aItem: AssignedProduct) {
        withContext(Dispatchers.IO) {
            database.updateAssignedProduct(aItem)
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