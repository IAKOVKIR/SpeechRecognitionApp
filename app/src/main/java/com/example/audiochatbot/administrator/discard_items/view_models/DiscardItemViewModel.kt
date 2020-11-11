package com.example.audiochatbot.administrator.discard_items.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.Time
import com.example.audiochatbot.database.models.AssignedProduct
import com.example.audiochatbot.database.models.DiscardedItem
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for DiscardItemFragment.
 *
 * @param adminId - the key of the current admin user we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - UserDao reference.
 */
class DiscardItemViewModel(private val adminId: Int, private val storeId: Int, val dataSource: UserDao) : ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

    /** Coroutine setup variables */

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
     * a [DiscardItemViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val products = database.getAllProductsLiveWithStoreID(storeId)

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

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
            else
                _message.value = "I am sorry I cannot understand your command"
        }
    }

    /**
     * method that discards the items
     */
    fun discardItem(productId: Int, quantity: Int, comment: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            if (quantity > 0) {
                val num = getQuantity(productId)
                if (num >= quantity) {
                    val aId = dItem(productId, adminId, quantity, comment)
                    val aItem = getAssignedProduct(aId)
                    aItem!!.quantity -= quantity
                    updateAssignedItem(aItem!!)
                    _closeFragment.value = true
                } else
                    _message.value = "the entered value is bigger then the quantity of the product"
            } else
                _message.value = "the value of the item is less than 1"
        }
    }

    /**
     * Suspending method that discards the item and returns the AssignedProduct Id
     */
    private suspend fun dItem(productId: Int, userId: Int, quantity: Int, comment: String): Int {
        return withContext(Dispatchers.IO) {
            val num = database.getLastDiscardedItemId() + 1
            val apId = database.getAssignedProductId(productId, storeId)
            val item = DiscardedItem(num, apId, userId, quantity, comment, time.getDate(), time.getTime())
            database.discardItem(item)
            apId
        }
    }

    /**
     * Suspending method that returns the quantity of the assigned product
     */
    private suspend fun getQuantity(productId: Int): Int {
        return withContext(Dispatchers.IO) {
            database.getAssignedProductQuantity(productId, storeId)
        }
    }

    /**
     * Suspending method that retrieves the AssignedProduct
     */
    private suspend fun getAssignedProduct(id: Int): AssignedProduct? {
        return withContext(Dispatchers.IO) {
            database.getAssignedProduct(id)
        }
    }

    /**
     * Suspending method that updates the AssignedProduct record
     */
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