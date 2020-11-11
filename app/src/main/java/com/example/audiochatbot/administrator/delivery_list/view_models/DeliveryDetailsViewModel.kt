package com.example.audiochatbot.administrator.delivery_list.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.database.models.*
import kotlinx.coroutines.*

/**
 * ViewModel for DeliveryDetailsFragment.
 *
 * @param userId - the key of the current user we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param deliveryId - the key of the current delivery we are working on.
 * @param dataSource - UserDao reference.
 */
class DeliveryDetailsViewModel(val userId: Int, val storeId: Int, val deliveryId: Int, val dataSource: UserDao): ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
     */
    private val database = dataSource

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
     * a [DeliveryDetailsViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of DeliveryProduct
     */
    private var _deliveryProducts = MutableLiveData<List<DeliveryProduct>>()
    val deliveryProducts: LiveData<List<DeliveryProduct>> get() = _deliveryProducts

    /**
     * Lifecycle-aware observable that stores the List of Strings for the report
     */
    private var _reportList = MutableLiveData<List<String>>()
    val reportList: LiveData<List<String>> get() = _reportList

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

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            //prepopulate the list of delivery products
            _deliveryProducts.value = getItems()
        }
    }

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = givenText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("download a report") || text.contains("download report") || text.contains("download the report"))
                generateAReport()
            else
                _message.value = "I am sorry, I cannot recognise your command"
        }
    }

    /**
     * method that generates a report of the delivery
     */
    fun generateAReport() {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val list = mutableListOf<String>()
            val del = getDelivery()
            val user = getUser(del.userId)
            val delStatus = getFirstRecord()

            list.add("Delivery $deliveryId Report")
            list.add("Created by: ${user.firstName} ${user.lastName} {id : ${user.userId}}")
            list.add("Date created: ${del.dateModified} | ${del.timeModified}")
            list.add("Status: ${del.status}")

            if (del.status == "Canceled") {
                if (delStatus != null && delStatus.userId != user.userId) {
                    val anotherUser = getUser(delStatus.userId)
                    list.add("Canceled by: ${anotherUser.firstName} ${anotherUser.lastName} {id : ${anotherUser.userId}}")
                } else {
                    list.add("Canceled by: ${user.firstName} ${user.lastName} {id : ${user.userId}}")
                }
            } else if (del.status == "Delivered") {
                if (delStatus != null && delStatus.userId != user.userId) {
                    val anotherUser = getUser(delStatus.userId)
                    list.add("Received by: ${anotherUser.firstName} ${anotherUser.lastName} {id : ${anotherUser.userId}}")
                } else {
                    list.add("Received by: ${user.firstName} ${user.lastName} {id : ${user.userId}}")
                }
            }

            list.add("Items:")

            val listAI = getAssignedItems()
            val products = getItemsDelivery()

            for (element in listAI) {
                for (j in products) {
                    if (element.productId == j.productId) {
                        for (m in deliveryProducts.value!!) {
                            if (m.assignedProductId == element.assignedProductId && (m.smallUnitQuantity != 0 || m.bigUnitQuantity != 0)) {
                                list.add("${j.name} {id : ${j.productId}} - [${j.smallUnitName}: ${m.smallUnitQuantity}], [${j.bigUnitName}: ${m.bigUnitQuantity}]")
                                break
                            }
                        }
                        break
                    }
                }
            }

            _reportList.value = list
        }
    }

    /**
     * Suspending method getAssignedItems that retrieves a list of the assigned items with storeId from database
     */
    private suspend fun getAssignedItems(): List<AssignedProduct> {
        return withContext(Dispatchers.IO) {
            database.getAssignedProductsList(storeId)
        }
    }

    /**
     * Suspending method that retrieves the list of delivery products with delivery Id
     */
    private suspend fun getItems(): List<DeliveryProduct> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveryProducts(deliveryId)
        }
    }

    /**
     * Suspending method that retrieves the delivery with delivery id
     */
    private suspend fun getDelivery(): Delivery {
        return withContext(Dispatchers.IO) {
            database.getDeliveryWithDeliveryId(deliveryId)
        }
    }

    /**
     * Suspending method that retrieves a user object with user Id
     */
    private suspend fun getUser(userId: Int): User {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userId)
        }
    }

    /**
     * method that retrieves the DeliveryProductStatus object
     */
    private suspend fun getFirstRecord(): DeliveryProductStatus? {
        return withContext(Dispatchers.IO) {
            database.getFirstDeliveryProductStatus(deliveryId)
        }
    }

    /**
     * Suspending method getItems that retrieves a list of the products with storeId from database
     */
    private suspend fun getItemsDelivery(): List<Product> {
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