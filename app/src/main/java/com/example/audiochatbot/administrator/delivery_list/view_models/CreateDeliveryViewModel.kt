package com.example.audiochatbot.administrator.delivery_list.view_models

import android.annotation.SuppressLint
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

    private var productIds: MutableList<Int> = arrayListOf()
    private var smallBigQuantities: MutableList<Int> = arrayListOf()

    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private var _l = MutableLiveData<List<Int>>()
    val l: LiveData<List<Int>> get() = _l

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    private val _isDone = MutableLiveData<Boolean>()
    val isDone
        get() = _isDone

    init {
        uiScope.launch {
            _products.value = getItems()
            for (element in products.value!!) {
                productIds.add(element.productId)
            }

            smallBigQuantities = List(productIds.size * 2) { 0 } as MutableList<Int>
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        uiScope.launch {
            val text = givenText.toLowerCase()
            Log.e("heh", text)
            if (text.contains("go back"))
                _closeFragment.value = true
            else {
                val matchAddItems = "add".toRegex().find(text)
                val matchRemoveItems = "remove items of".toRegex().find(text)
                val indexAddItems = matchAddItems?.range?.last
                val indexRemoveItems = matchRemoveItems?.range?.last

                if (indexAddItems != null) {
                    val matchSmallUnits = "small unit".toRegex().find(text)
                    val matchBigUnits = "big unit".toRegex().find(text)
                    val indexSmallUnits = matchSmallUnits?.range
                    val indexBigUnits = matchBigUnits?.range
                    var smallQuantity = 0
                    var bigQuantity = 0
                    var id = -1

                    if (indexSmallUnits != null) {
                        if (indexSmallUnits.first > indexAddItems) {
                            var lastIndex = indexSmallUnits.last + 1
                            val str = text.substring(indexAddItems + 1, indexAddItems + 7)
                            smallQuantity = textToInteger(str)
                            Log.e("heh s", "$smallQuantity")

                            if (indexBigUnits != null) {
                                if (indexBigUnits.first > indexSmallUnits.last) {
                                    val strBig = text.substring(lastIndex, indexBigUnits.first)
                                    lastIndex = indexBigUnits.last + 1
                                    bigQuantity = textToInteger(strBig)
                                    Log.e("heh b", "$bigQuantity")

                                }
                            }

                            val strProduct = text.substring(lastIndex)
                            val matchProductName = "of".toRegex().find(strProduct)
                            val matchProductId = "of product number".toRegex().find(strProduct)
                            val indexProductName = matchProductName?.range
                            val indexProductId = matchProductId?.range

                            if (smallQuantity > 0 || bigQuantity > 0) {
                                if (indexProductName != null) {
                                    val strName = text.substring(indexProductName.last + 1)

                                    val list = products.value

                                    if (list != null) {
                                        for (i in list) {
                                            Log.e(i.name.toLowerCase(), strName)
                                            if (strName.contains(i.name.toLowerCase())) {
                                                id = i.productId
                                                break
                                            }
                                        }
                                    }

                                } else if (indexProductId != null) {
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
                                    }
                                }
                            }
                        }
                    } else if (indexBigUnits != null) {
                        if (indexBigUnits.first > indexAddItems) {
                            val strBig = text.substring(indexAddItems, indexBigUnits.last + 1)
                            bigQuantity = textToInteger(strBig)
                            Log.e("heh b", "$bigQuantity")

                            val strProduct = text.substring(indexBigUnits.last + 1)
                            val matchProductName = "of".toRegex().find(strProduct)
                            val matchProductId = "of product number".toRegex().find(strProduct)
                            val indexProductName = matchProductName?.range
                            val indexProductId = matchProductId?.range

                            if (bigQuantity > 0) {
                                if (indexProductName != null) {
                                    val strName = text.substring(indexProductName.last + 1)

                                    val list = products.value

                                    if (list != null) {
                                        for (i in list) {
                                            Log.e(i.name.toLowerCase(), strName)
                                            if (strName.contains(i.name.toLowerCase())) {
                                                id = i.productId
                                                break
                                            }
                                        }
                                    }

                                } else if (indexProductId != null) {
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
                                    }
                                }
                            }
                        }
                    }

                    if (id > 0) {
                        if (smallQuantity > 0 || bigQuantity > 0) {
                            addItem(id, smallQuantity, bigQuantity)
                        }
                    }

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
                            }
                        }
                    } else {
                        val str = text.substring(indexRemoveItems + 1)
                        val list = products.value
                        var num1 = -1

                        if (list != null) {
                            for (i in list) {
                                Log.e(i.name.toLowerCase(), str)
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
                }
            }
        }
    }

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

            smallBigQuantities[num * 2] = 0
            smallBigQuantities[num * 2 + 1] = 0

            _l.value = smallBigQuantities.toList()
            _products.value = getItems()

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
                    if (element.productId == productIds[j] && (smallBigQuantities[j * 2] != 0 || smallBigQuantities[j * 2 + 1] != 0)) {
                        itemList.add(DeliveryProduct(deliveryId, productIds[j], smallBigQuantities[j * 2], smallBigQuantities[j * 2 + 1], "not available"))
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

    private fun textToInteger(str: String): Int {
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> {
                Log.e("heh", result)
                result.toInt()
            }
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> 0
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