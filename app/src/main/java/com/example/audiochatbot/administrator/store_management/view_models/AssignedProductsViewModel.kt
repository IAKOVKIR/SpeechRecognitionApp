package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.AssignedProduct
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class AssignedProductsViewModel(val storeId: Int, private val database: UserDao): ViewModel() {

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
     * a [AssignedProductsViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val products = database.getAllProductsLiveWithStoreID(storeId)

    private val _navigateToProductDetails = MutableLiveData<Int>()
    val navigateToProductDetails
        get() = _navigateToProductDetails

    private val _navigateToAssignProducts = MutableLiveData<Boolean>()
    val navigateToAssignProducts
        get() = _navigateToAssignProducts

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        uiScope.launch {
            val text = recordedText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("assign products") || text.contains("assign product") || text.contains("assign a product")
                || text.contains("assign the product"))
                _navigateToAssignProducts.value = true
            else {
                val matchRemoveProductNumber = "remove product number".toRegex().find(text)
                val matchRemoveProductName = "remove".toRegex().find(text)

                val indexRemoveProductNumber = matchRemoveProductNumber?.range?.last
                val indexRemoveProductName = matchRemoveProductName?.range?.last

                if (indexRemoveProductNumber != null) {
                    val num = textToInteger(text, indexRemoveProductNumber)

                    if (num > 0) {
                        val list = products.value
                        var res = false

                        if (list != null) {
                            for (i in list) {
                                if (i.productId == num) {
                                    res = true
                                    break
                                }
                            }

                            if (res)
                                deleteRecord(num)
                            else
                                _message.value = "You do not have an access to this product"
                        } else
                            _message.value = "Product list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else if (indexRemoveProductName != null) {
                    val str = text.substring(indexRemoveProductName + 1)
                    val list = products.value
                    var num = -1

                    if (list != null) {
                        for (i in list) {
                            if (str.contains(i.name.toLowerCase())) {
                                num = i.productId
                                break
                            }
                        }

                        if (num != -1)
                            deleteRecord(num)
                        else
                            _message.value = "You do not have an access to this product"
                    } else
                        _message.value = "Product list is empty"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    private fun textToInteger(text: String, lastIndex: Int): Int {
        val str = text.substring(lastIndex + 1)
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> result.toInt()
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> -1
        }
    }

    fun onProductClicked(id: Int) {
        _navigateToProductDetails.value = id
    }

    fun onProductNavigated() {
        _navigateToProductDetails.value = null
        _message.value = null
        _closeFragment.value = null
        _navigateToAssignProducts.value = null
    }

    fun deleteRecord(productId: Int) {
        uiScope.launch {
            deleteRecordDb(productId)
            val deletedProducts = getRecordDb(productId)
            if (deletedProducts != null)
                _message.value = "The product was removed successfully "
            else
                _message.value = "Something went wrong"
        }
    }

    private suspend fun deleteRecordDb(productId: Int) {
        withContext(Dispatchers.IO) {
            database.removeProductFromStore(productId, storeId)
        }
    }

    private suspend fun getRecordDb(productId: Int) : AssignedProduct? {
        return withContext(Dispatchers.IO) {
            database.getAssignedProduct(productId, storeId)
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