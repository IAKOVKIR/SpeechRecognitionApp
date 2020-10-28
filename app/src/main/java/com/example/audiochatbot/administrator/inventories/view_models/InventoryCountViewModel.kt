package com.example.audiochatbot.administrator.inventories.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.*
import kotlinx.coroutines.*
import java.lang.NumberFormatException

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

    private var _earnedAmount = MutableLiveData<Float>()
    val earnedAmount get() = _earnedAmount

    private var _submit = MutableLiveData<Boolean>()
    val submit get() = _submit

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    private val _isDone = MutableLiveData<Boolean>()
    val isDone
        get() = _isDone

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

    /**
     * Method that analyses the string and do the actions based on the command that was found in the string
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        uiScope.launch {
            val text = givenText.toLowerCase()
            // if the command is go back
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("submit the inventory count") || text.contains("submit the inventor account") ||
                text.contains("submit inventory count") || text.contains("submit inventor account")) {
                _submit.value = true
            } else {
                // get the last indexes of the given substrings
                val matchAddItems = "add".toRegex().find(text)
                val matchAtItems = "at".toRegex().find(text)
                val matchRemoveItems = "remove items of".toRegex().find(text)
                val matchAmountOfEarnings = "the amount of earnings is".toRegex().find(text)

                val indexAddItems = matchAddItems?.range?.last
                val indexAtItems = matchAtItems?.range?.last
                val indexRemoveItems = matchRemoveItems?.range?.last
                val indexAmountOfEarnings = matchAmountOfEarnings?.range?.last

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
                        val indexNum1 = indexAddItems ?: indexAtItems
                        if (indexBigUnits.first > indexNum1!!) {
                            val strBig = text.substring(indexNum1, indexBigUnits.last + 1)
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
                } else if (indexAmountOfEarnings != null) {
                    val str = text.substring(indexAmountOfEarnings + 1)

                    val num = try {
                        val result = str.toFloat()
                        result.round(2)
                    } catch (e: NumberFormatException) {
                        textToInteger(str).toFloat()
                    }

                    if (num >= 0) {
                        _earnedAmount.value = num
                    }
                } else
                    _message.value = "I'm sorry, I cannot understand your command"
            }
        }
    }

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
        }
    }

    fun submitInventoryCount(currentEarnings: Float) {
        uiScope.launch {
            val list = getAssignedItems()
            var totalPrice = 0F

            for (element in list) {
                for (j in 0 until productObjects.size) {
                    if (element.productId == productObjects[j].productId) {
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

                        val newQuantity =
                            (smallQuantity * smallBigQuantities[j * 2]) + (bigQuantity * smallBigQuantities[j * 2 + 1])
                        val difference = element.quantity - newQuantity
                        if (difference >= 0) {
                            element.quantity = newQuantity
                            totalPrice += productObjects[j].price * difference * (1F - (element.sale / 100F))
                        }
                        break
                    }
                }
            }

            for (element in list) {
                updateAssignedItem(element)
            }

            val storeObj = getStore(storeId)
            storeObj.cashOnHand = storeObj.cashOnHand + totalPrice

            updateStore(storeObj)

            val lastId = getLastId()
            finishCount(InventoryCount(lastId + 1, storeId, adminId, storeObj.cashOnHand, currentEarnings, "20/09/2020", "14:00"))

            _isDone.value = true
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
            str.contains("zero") -> 0
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") || str.contains("four") -> 4
            else -> -1
        }
    }

    private fun Float.round(decimals: Int): Float {
        var multiplier = 1F
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
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

    private suspend fun getStore(id: Int): Store {
        return withContext(Dispatchers.IO) {
            database.getStoreWithId(id)
        }
    }

    private suspend fun updateStore(store: Store) {
        withContext(Dispatchers.IO) {
            database.updateStore(store)
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