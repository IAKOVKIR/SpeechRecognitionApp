package com.example.audiochatbot.delivery_user.delivery_list.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.Time
import com.example.audiochatbot.database.*
import com.example.audiochatbot.database.models.*
import kotlinx.coroutines.*

/**
 * ViewModel for DeliveryUserListDetailsFragment.
 *
 * @param deliveryId - the key of the current delivery we are working on.
 * @param database - the reference to UniDatabase
 */
class DeliveryUserListDetailsViewModel(val storeId: Int, val adminId: Int, val deliveryId: Int, private val database: UserDao): ViewModel() {

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
     * a [DeliveryUserListDetailsViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var productIds: MutableList<Int> = arrayListOf()
    private var smallBigQuantities: MutableList<Int> = arrayListOf()

    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private var _reportList = MutableLiveData<List<String>>()
    val reportList: LiveData<List<String>> get() = _reportList

    private var _l = MutableLiveData<List<Int>>()
    val l: LiveData<List<Int>> get() = _l

    private var deliveryProducts: List<DeliveryProduct>? = null

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            _products.value = getItems()
            deliveryProducts = getDeliveryItems()
            val assignedProducts = getAssignedItemsWithDeliveryId()
            for (element in products.value!!) {
                productIds.add(element.productId)
            }

            // generate a mutable list with double size of productIds and set all values as 0
            smallBigQuantities = List(productIds.size * 2) { 0 } as MutableList<Int>

            for (i in productIds) {
                for (j in assignedProducts) {
                    if (i == j.productId) {
                        for (k in deliveryProducts!!) {
                            if (k.assignedProductId == j.assignedProductId) {
                                addItem(i, k.smallUnitQuantity, k.bigUnitQuantity)
                                break
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    /**
     * Method that analyses the string and do the actions based on the command that was found in the string
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = givenText.toLowerCase()
            // if the command is go back
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("update the delivery") || text.contains("update delivery") || text.contains("update this delivery")
                || text.contains("update a delivery")) {
                submitDelivery()
            } else if (text.contains("download a report") || text.contains("download report") || text.contains("download the report")) {
                    generateAReport()
            } else {
                // get the last indexes of the given substrings
                val matchAddItems = "add".toRegex().find(text)
                val matchAtItems = "at".toRegex().find(text)
                val matchRemoveItems = "remove items of".toRegex().find(text)
                val indexAddItems = matchAddItems?.range?.last
                val indexAtItems = matchAtItems?.range?.last
                val indexRemoveItems = matchRemoveItems?.range?.last


                if (indexAddItems != null || indexAtItems != null) {
                    // get the ranges of the given substrings
                    val matchSmallUnits = "small unit".toRegex().find(text)
                    val matchBigUnits = "big unit".toRegex().find(text)
                    val indexSmallUnits = matchSmallUnits?.range
                    val indexBigUnits = matchBigUnits?.range
                    val indexNum = indexAddItems ?: indexAtItems
                    var smallQuantity = 0
                    var bigQuantity = 0
                    var id = -1

                    if (indexSmallUnits != null) {
                        if (indexSmallUnits.first > indexNum!!) {
                            var lastIndex = indexSmallUnits.last + 1
                            val str = text.substring(indexNum + 1, indexNum + 7)
                            smallQuantity = textToInteger(str)

                            if (indexBigUnits != null) {
                                if (indexBigUnits.first > indexSmallUnits.last) {
                                    val strBig = text.substring(lastIndex, indexBigUnits.first)
                                    lastIndex = indexBigUnits.last + 1
                                    bigQuantity = textToInteger(strBig)
                                }
                            }

                            val strProduct = text.substring(lastIndex)
                            val matchProductName = "of".toRegex().find(strProduct)
                            val matchProductId = "of product number".toRegex().find(strProduct)
                            val indexProductName = matchProductName?.range
                            val indexProductId = matchProductId?.range

                            if (smallQuantity > 0 || bigQuantity > 0) {
                                if (indexProductId != null) {
                                    val strId = strProduct.substring(indexProductId.last + 1)
                                    val testId = textToInteger(strId)
                                    val list = products.value

                                    if (list != null) {
                                        for (i in list) {
                                            if (i.productId == testId) {
                                                id = testId
                                                break
                                            }
                                        }

                                        if (id > 0) {
                                            if (smallQuantity > 0 || bigQuantity > 0) {
                                                addItem(id, smallQuantity, bigQuantity)
                                            } else
                                                _message.value = "The total quantity of the product is less than zero"
                                        } else
                                            _message.value = "I'm sorry, I cannot understand your command"
                                    } else
                                        _message.value = "Items are not available"
                                } else if (indexProductName != null) {
                                    val strName = strProduct.substring(indexProductName.last + 1)
                                    val list = products.value

                                    if (list != null) {
                                        for (i in list) {
                                            if (strName.contains(i.name.toLowerCase())) {
                                                id = i.productId
                                                break
                                            }
                                        }

                                        if (id > 0) {
                                            if (smallQuantity > 0 || bigQuantity > 0) {
                                                addItem(id, smallQuantity, bigQuantity)
                                            } else
                                                _message.value = "The total quantity of the product is less than zero"
                                        } else
                                            _message.value = "I'm sorry, I cannot understand your command"
                                    } else
                                        _message.value = "Items are not available"
                                } else
                                    _message.value = "I'm sorry, I cannot understand your command"
                            } else
                                _message.value = "I'm sorry, I cannot understand your command"
                        } else
                            _message.value = "I'm sorry, I cannot understand your command"
                    } else if (indexBigUnits != null) {
                        if (indexBigUnits.first > indexNum!!) {
                            val strBig = text.substring(indexNum, indexBigUnits.last + 1)
                            bigQuantity = textToInteger(strBig)

                            val strProduct = text.substring(indexBigUnits.last + 1)
                            val matchProductName = "of".toRegex().find(strProduct)
                            val matchProductId = "of product number".toRegex().find(strProduct)
                            val indexProductName = matchProductName?.range
                            val indexProductId = matchProductId?.range

                            if (bigQuantity > 0) {
                                if (indexProductId != null) {
                                    val strId = text.substring(indexProductId.last + 1)
                                    val testId = textToInteger(strId)
                                    val list = products.value

                                    if (list != null) {
                                        for (i in list) {
                                            if (i.productId == testId) {
                                                id = testId
                                                break
                                            }
                                        }

                                        if (id > 0) {
                                            if (smallQuantity > 0 || bigQuantity > 0) {
                                                addItem(id, smallQuantity, bigQuantity)
                                            } else
                                                _message.value = "The total quantity of the product is less than zero"
                                        } else
                                            _message.value = "I'm sorry, I cannot understand your command"
                                    } else
                                        _message.value = "Items are not available"
                                } else if (indexProductName != null) {
                                    val strName = text.substring(indexProductName.last + 1)

                                    val list = products.value

                                    if (list != null) {
                                        for (i in list) {
                                            if (strName.contains(i.name.toLowerCase())) {
                                                id = i.productId
                                                break
                                            }
                                        }

                                        if (id > 0) {
                                            if (smallQuantity > 0 || bigQuantity > 0) {
                                                addItem(id, smallQuantity, bigQuantity)
                                            } else
                                                _message.value = "The total quantity of the product is less than zero"
                                        } else
                                            _message.value = "I'm sorry, I cannot understand your command"
                                    } else
                                        _message.value = "Items are not available"
                                } else
                                    _message.value = "I'm sorry, I cannot understand your command"
                            } else
                                _message.value = "I'm sorry, I cannot understand your command"
                        } else
                            _message.value = "I'm sorry, I cannot understand your command"
                    } else
                        _message.value = "I'm sorry, I cannot understand your command"

                } else if (indexRemoveItems != null) {
                    val matchProductId = "product number".toRegex().find(text)
                    val indexProductId = matchProductId?.range

                    if (indexProductId != null) {
                        if (indexProductId.first > indexRemoveItems) {
                            val str = text.substring(indexProductId.last + 1)
                            val num = textToInteger(str)

                            if (num > 0) {
                                val list = products.value
                                var num1 = -1

                                if (list != null) {
                                    for (i in list) {
                                        if (i.productId == num) {
                                            num1 = num
                                            break
                                        }
                                    }

                                    if (num1 > 0)
                                        removeItem(num1)
                                    else
                                        _message.value =
                                            "You do not have an access to this product"
                                } else
                                    _message.value = "Items are not available"
                            } else
                                _message.value = "I'm sorry, I cannot understand your command"
                        } else
                            _message.value = "I'm sorry, I cannot understand your command"
                    } else {
                        val str = text.substring(indexRemoveItems + 1)
                        val list = products.value
                        var num1 = -1

                        if (list != null) {
                            for (i in list) {
                                if (str.contains(i.name.toLowerCase())) {
                                    num1 = i.productId
                                    break
                                }
                            }

                            if (num1 > 0)
                                removeItem(num1)
                            else
                                _message.value =
                                    "You do not have an access to this product"
                        } else
                            _message.value = "Items are not available"
                    }
                } else
                    _message.value = "I'm sorry, I cannot understand your command"
            }
        }
    }

    /**
     * Method addItem updates the item of the list with entered quantities
     */
    fun addItem(productId: Int, smallQuantity: Int, bigQuantity: Int) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            // Items will be added if the entered total quantity
            // (small unit and big unit quantities combined) is higher than zero.
            if (smallQuantity > 0 || bigQuantity > 0) {
                // get the index of productId in the list
                val num = productIds.indexOf(productId)
                // assign new values
                smallBigQuantities[num * 2] = smallQuantity
                smallBigQuantities[num * 2 + 1] = bigQuantity

                // update the lists
                _l.value = smallBigQuantities.toList()
                _products.value = getItems()
            } else
                _message.value = "The total quantity has to be higher than zero."
        }
    }

    /**
     * Method removeItem removes the quantities of the product
     */
    fun removeItem(productId: Int) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            // get the index of productId in the list
            val num = productIds.indexOf(productId)
            if (smallBigQuantities[num * 2] > 0 || smallBigQuantities[num * 2 + 1] > 0) {
                // assign new values
                smallBigQuantities[num * 2] = 0
                smallBigQuantities[num * 2 + 1] = 0

                // update the lists
                _l.value = smallBigQuantities.toList()
                _products.value = getItems()
            } else
                _message.value = "The total quantity of the product is equal to zero"
        }
    }

    /**
     * Method submitDelivery inserts a new delivery with all added items
     */
    fun submitDelivery() {
        //launch a new coroutine in background and continue
        uiScope.launch {
            // declare a mutable list for items that has a total quantity higher than zero
            val itemList: MutableList<DeliveryProduct> = arrayListOf()
            var newId = getLastDeliveryProductId()
            val list = getAssignedItems()

            for (element in list) {
                for (j in 0 until productIds.size) {
                    // if productIds are equal and total quantity os higher than 0, then it adds
                    // a new DeliveryProduct object to the list and terminates the nearest enclosing loop
                    if (element.productId == productIds[j] && (smallBigQuantities[j * 2] != 0 || smallBigQuantities[j * 2 + 1] != 0)) {
                        newId++
                        itemList.add(DeliveryProduct(newId, deliveryId, element.assignedProductId, smallBigQuantities[j * 2], smallBigQuantities[j * 2 + 1]))
                        break
                    }
                }
            }

            if (itemList.size != 0) {
                clearDeliveryItems()

                val delivery = Delivery(deliveryId, storeId, adminId, "In Transit", time.getDate(), time.getTime())
                addNewDelivery(delivery)

                val newList = itemList.toList()
                addNewItems(newList)

                _closeFragment.value = true
            } else
                _message.value = "The delivery list is empty"
        }
    }

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
            }

            list.add("Items:")

            val listAI = getAssignedItems()

            for (element in listAI) {
                for (j in products.value!!) {
                    if (element.productId == j.productId) {
                        for (m in deliveryProducts!!) {
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
     * Method textToInteger that returns the int value from the given string
     */
    private fun textToInteger(str: String): Int {
        // gets the string of numbers that were found in the string
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> result.toInt()
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> 0
        }
    }

    /**
     * Suspending method getLastDeliveryProductId that retrieves the id of the last delivery product from the database
     */
    private suspend fun getLastDeliveryProductId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastDeliveryProductId()
        }
    }

    /**
     * Suspending method addNewDelivery that inserts a new delivery into the database
     */
    private suspend fun addNewDelivery(delivery: Delivery) {
        withContext(Dispatchers.IO) {
            database.insertDelivery(delivery)
        }
    }

    /**
     * Suspending method addNewItems that inserts a list of delivery products into the database
     */
    private suspend fun addNewItems(itemList: List<DeliveryProduct>) {
        withContext(Dispatchers.IO) {
            database.insertDeliveryProducts(itemList)
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
     * Suspending method getAssignedItemsWithDeliveryId that retrieves a list of the assigned items with deliveryId from database
     */
    private suspend fun getAssignedItemsWithDeliveryId(): List<AssignedProduct> {
        return withContext(Dispatchers.IO) {
            database.getAssignedProductsWithDeliveryId(deliveryId)
        }
    }

    /**
     * Suspending method getItems that retrieves a list of the products with storeId from database
     */
    private suspend fun getItems(): List<Product> {
        return withContext(Dispatchers.IO) {
            database.getAllProductsWithStoreID(storeId)
        }
    }

    /**
     * Suspending method getDeliveryItems that retrieves a list of the products with deliveryId from database
     */
    private suspend fun getDeliveryItems(): List<DeliveryProduct> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveryProducts(deliveryId)
        }
    }

    /**
     * Suspending method clearDeliveryItems that clears a list of the delivery products with deliveryId from database
     */
    private suspend fun clearDeliveryItems() {
        withContext(Dispatchers.IO) {
            database.deleteDeliveryProductRecord(deliveryId)
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

    fun setMessage(txt: String) {
        _message.value = txt
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