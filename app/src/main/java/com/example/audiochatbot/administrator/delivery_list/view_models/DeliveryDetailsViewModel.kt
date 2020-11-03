package com.example.audiochatbot.administrator.delivery_list.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.database.models.*
import kotlinx.coroutines.*

class DeliveryDetailsViewModel(val userId: Int, val storeId: Int, val deliveryId: Int, private val database: UserDao): ViewModel() {

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

    private var _reportList = MutableLiveData<List<String>>()
    val reportList: LiveData<List<String>> get() = _reportList

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

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        uiScope.launch {
            val text = givenText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else
                _message.value = "I am sorry, I cannot recognise your command"
        }
    }

    fun generateAReport() {
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

    private suspend fun getItems(): List<DeliveryProduct> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveryProducts(deliveryId)
        }
    }

    private suspend fun getDelivery(): Delivery {
        return withContext(Dispatchers.IO) {
            database.getDeliveryWithDeliveryId(deliveryId)
        }
    }

    private suspend fun getUser(userId: Int): User {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userId)
        }
    }

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