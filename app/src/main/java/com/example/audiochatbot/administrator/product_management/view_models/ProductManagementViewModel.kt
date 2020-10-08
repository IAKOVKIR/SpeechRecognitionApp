package com.example.audiochatbot.administrator.product_management.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.user_management.view_models.UserManagementViewModel
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for ProductManagementFragment.
 * @param businessId - the key of the current business we are working on.
 * @param dataSource -
 */
class ProductManagementViewModel(private val businessId: Int, private val dataSource: UserDao): ViewModel() {

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
     * a [UserManagementViewModel] update the UI after performing some processing.
     */

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _navigateToProductDetails = MutableLiveData<Int>()
    val navigateToProductDetails
        get() = _navigateToProductDetails

    private val _navigateToCreateNewProduct = MutableLiveData<Boolean>()
    val navigateToCreateNewProduct get() = _navigateToCreateNewProduct

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        uiScope.launch {
            _products.value = getAllProducts(businessId)
        }
    }

    fun convertStringToAction(text: String) {
        uiScope.launch {
            Log.e("heh", text)
            if (text.contains("go back"))
                _closeFragment.value = true
            else if (text.contains("add new product") || text.contains("create new product")) {
                _navigateToCreateNewProduct.value = true
            } else {
                val patternOpenProductNumber = "open product number".toRegex()
                val patternCancelDelivery = "cancel delivery number".toRegex()

                val matchOpenProductNumber = patternOpenProductNumber.find(text)
                val matchCancelDelivery = patternCancelDelivery.find(text)

                val indexOpenProductNumber = matchOpenProductNumber?.range?.last
                val indexCancelDelivery = matchCancelDelivery?.range?.last

                if (indexOpenProductNumber != null) {
                    val num = textToInteger(text, indexOpenProductNumber)

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
                                _navigateToProductDetails.value = num
                            else
                                _message.value = "You do not have an access to this product"
                        } else
                            _message.value = "Product list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                }
            }
        }
    }

    private fun textToInteger(text: String, lastIndex: Int): Int {
        val str = text.substring(lastIndex + 1)
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> {
                Log.e("heh", result)
                result.toInt()
            }
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("for") -> 4
            else -> -1
        }
    }

    fun retrieveList(str: String) {
        uiScope.launch {
            _products.value = getListWithString("%$str%", businessId)
        }
    }

    fun onProductClicked(id: Int) {
        _navigateToProductDetails.value = id
    }

    fun onProductNavigated() {
        _navigateToCreateNewProduct.value = null
        _navigateToProductDetails.value = null
        _message.value = null
        _closeFragment.value = null
    }

    private suspend fun getAllProducts(businessId: Int): List<Product> {
        return withContext(Dispatchers.IO) {
            dataSource.getAllProductsWithBusinessId(businessId)
        }
    }

    private suspend fun getListWithString(line: String, businessId: Int): List<Product> {
        return withContext(Dispatchers.IO) {
            dataSource.getAllProductsWithString(line, businessId)
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