package com.example.audiochatbot.administrator.delivery_list.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.Time
import com.example.audiochatbot.database.*
import com.example.audiochatbot.database.models.AssignedProduct
import com.example.audiochatbot.database.models.Delivery
import com.example.audiochatbot.database.models.DeliveryProduct
import com.example.audiochatbot.database.models.Product
import kotlinx.coroutines.*

/**
 * ViewModel for DeliveryDetailsFragment.
 *
 * @param adminId - the key of the current admin user we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - UserDao reference.
 */
class CreateDeliveryViewModel(val storeId: Int, val adminId: Int, private val dataSource: UserDao): ViewModel() {

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
     * a [CreateDeliveryViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private var productIds: MutableList<Int> = arrayListOf()
    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private var smallBigQuantities: MutableList<Int> = arrayListOf()

    /**
     * Lifecycle-aware observable that stores the list of Product
     */
    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private var _l = MutableLiveData<List<Int>>()
    val l: LiveData<List<Int>> get() = _l

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Boolean
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            //prepopulate the lists
            _products.value = getItems()
            for (element in products.value!!) {
                productIds.add(element.productId)
            }

            // generate a mutable list with double size of productIds and set all values as 0
            smallBigQuantities = List(productIds.size * 2) { 0 } as MutableList<Int>
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
            else if (text.contains("submit the delivery") || text.contains("submit delivery") || text.contains("submit") ||
                    text.contains("submit this delivery") || text.contains("submit a delivery")) {
                submitDelivery()
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
            val deliveryId = getLastDeliveryId() + 1
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
                val delivery = Delivery(deliveryId, storeId, adminId, "In Transit", time.getDate(), time.getTime())
                addNewDelivery(delivery)

                val newList = itemList.toList()
                addNewItems(newList)

                val checkId = getLastDeliveryId()
                if (checkId == deliveryId)
                    _closeFragment.value = true
                else
                    _message.value = "Something went wrong"
            } else
                _message.value = "The delivery list is empty"
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
     * Suspending method getLastDeliveryId that retrieves the id of the last delivery from the database
     */
    private suspend fun getLastDeliveryId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastDeliveryId()
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
     * Suspending method getItems that retrieves a list of the products with storeId from database
     */
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