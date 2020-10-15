package com.example.audiochatbot.administrator.discard_items.view_models

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.AssignedProduct
import com.example.audiochatbot.database.DiscardedItem
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class DiscardItemViewModel(private val adminId: Int, private val storeId: Int, val database: UserDao) : ViewModel() {

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
     * a [DiscardItemViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val products = database.getAllProductsLiveWithStoreID(storeId)

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(text: String) {
        uiScope.launch {
            Log.e("heh", text)
            if (text.contains("go back"))
                _closeFragment.value = true
            else {
                val patternDiscard = "discard".toRegex()
                val patternProductName = "items of".toRegex()
                val patternOneProductName = "discard one item of".toRegex()
                val patternProductId = "items of product number".toRegex()
                val patternOneProductId = "discard one item of product number".toRegex()

                val matchDiscard = patternDiscard.find(text)
                val matchProductName = patternProductName.find(text)
                val matchOneProductName = patternOneProductName.find(text)
                val matchProductId = patternProductId.find(text)
                val matchOneProductId = patternOneProductId.find(text)

                val indexDiscard = matchDiscard?.range?.last
                val indexProductName = matchProductName?.range
                val indexOneProductName = matchOneProductName?.range?.last
                val indexProductId = matchProductId?.range
                val indexOneProductId = matchOneProductId?.range

                if (indexDiscard != null) {
                    if (indexProductId != null) {
                        Log.e("step 0", "passed")
                        val txt = text.substring(indexProductId.last + 1).trim()
                        val txtId = txt.filter { it.isDigit() }
                        val list = products.value?.toList()
                        var itemId = -1

                        if (list != null) {
                            Log.e("step 1", txt)
                            val num = when {
                                txtId != "" -> txtId.toInt()
                                txt.contains("one") -> 1
                                txt.contains("to") || txt.contains("two") -> 2
                                txt.contains("for") -> 4
                                else -> -1
                            }

                            for (i in list.indices) {
                                if (num == list[i].productId) {
                                    itemId = num
                                    break
                                }
                            }

                            if (itemId != -1) {
                                Log.e("step 2", "passed $itemId")
                                val newTxt = text.substring(indexDiscard, indexProductId.first)
                                val result = newTxt.filter { it.isDigit() }

                                when {
                                    result != "" -> discardItem(itemId, result.toInt())
                                    newTxt.contains("one") -> discardItem(itemId, 1)
                                    newTxt.contains("two") -> discardItem(itemId, 2)
                                    newTxt.contains("for") || newTxt.contains("four") -> discardItem(itemId, 4)
                                    else -> _message.value = "Can't recognise your command"
                                }
                            } else
                                _message.value = "Can't recognise your command"
                        } else
                            _message.value = "Can't recognise your command"
                    } else if (indexOneProductId != null) {
                        val txt = text.substring(indexOneProductId.last + 1).trim()
                        val txtId = txt.filter { it.isDigit() }
                        val list = products.value?.toList()
                        var itemId = -1

                        if (list != null) {
                            Log.e("step 1", txt)
                            val num = when {
                                txtId != "" -> txtId.toInt()
                                txt.contains("one") -> 1
                                txt.contains("to") || txt.contains("two") -> 2
                                txt.contains("for") -> 4
                                else -> -1
                            }

                            for (i in list.indices) {
                                if (num == list[i].productId) {
                                    itemId = num
                                    break
                                }
                            }

                            if (itemId != -1) {
                                Log.e("step 2", "passed $itemId")
                                discardItem(itemId, 1)
                            } else
                                _message.value = "Can't recognise your command"
                        } else
                            _message.value = "Can't recognise your command"
                    } else if (indexProductName != null) {
                        //Log.e("step 1", "passed")
                        val txt = text.substring(indexProductName.last + 1).trim().toLowerCase()
                        val list = products.value?.toList()
                        var itemId = -1

                        if (list != null) {
                            for (i in list.indices) {
                                if (txt.contains(list[i].name.toLowerCase())) {
                                    itemId = list[i].productId
                                    break
                                }
                            }

                            if (itemId != -1) {
                                //Log.e("step 2", "passed")
                                val newTxt = text.substring(indexDiscard, indexProductName.first)
                                val result = newTxt.filter { it.isDigit() }

                                when {
                                    result != "" -> discardItem(itemId, result.toInt())
                                    text.contains("one") -> discardItem(itemId, 1)
                                    text.contains("two") -> discardItem(itemId, 2)
                                    text.contains("for") -> discardItem(itemId, 4)
                                    else -> _message.value = "Can't recognise your command"
                                }
                            } else
                                _message.value = "Can't recognise your command"
                        } else
                            _message.value = "Can't recognise your command"
                    } else if (indexOneProductName != null) {
                        val txt = text.substring(indexOneProductName + 1).trim().toLowerCase()
                        val list = products.value?.toList()
                        var itemId = -1

                        if (list != null) {
                            for (i in list.indices) {
                                if (txt.contains(list[i].name.toLowerCase())) {
                                    itemId = list[i].productId
                                    break
                                }
                            }

                            if (itemId != -1)
                                discardItem(itemId, 1)
                            else
                                _message.value = "The store doesn't have this product"
                        } else
                            _message.value = "The store doesn't have any products"
                    } else
                        _message.value = "Can't recognise your command"
                } else
                    _message.value = "Can't recognise your command"
            }
        }
    }

    fun discardItem(productId: Int, quantity: Int) {
        uiScope.launch {
            if (quantity > 0) {
                val num = getQuantity(productId)
                if (num >= quantity) {
                    val aId = dItem(productId, adminId, quantity)
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