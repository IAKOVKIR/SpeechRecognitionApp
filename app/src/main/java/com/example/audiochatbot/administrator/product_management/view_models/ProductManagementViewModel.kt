package com.example.audiochatbot.administrator.product_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for ProductManagementFragment.
 *
 * @param businessId - the key of the current delivery we are working on.
 * @param dataSource - UserDao reference.
 */
class ProductManagementViewModel(private val businessId: Int, private val dataSource: UserDao): ViewModel() {

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
     * a [ProductManagementViewModel] update the UI after performing some processing.
     */

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the List of Product
     */
    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private val _navigateToProductDetails = MutableLiveData<Int>()
    val navigateToProductDetails
        get() = _navigateToProductDetails

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _navigateToCreateNewProduct = MutableLiveData<Boolean>()
    val navigateToCreateNewProduct get() = _navigateToCreateNewProduct

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            _products.value = getAllProducts(businessId)
        }
    }

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = recordedText.toLowerCase()
            if (text.contains("go back"))
                _closeFragment.value = true
            else if (text.contains("add new product") || text.contains("create new product") || text.contains("add product")
                || text.contains("add a product") || text.contains("create product") || text.contains("create a product") ||
                text.contains("add a new product") || text.contains("create a new product")) {
                _navigateToCreateNewProduct.value = true
            } else {
                val patternOpenProductNumber = "open product number".toRegex()
                val patternOpenProduct = "open".toRegex()
                val patternFindProduct = "find".toRegex()

                val matchOpenProductNumber = patternOpenProductNumber.find(text)
                val matchOpenProduct = patternOpenProduct.find(text)
                val matchFindProduct = patternFindProduct.find(text)

                val indexOpenProductNumber = matchOpenProductNumber?.range?.last
                val indexOpenProduct = matchOpenProduct?.range?.last
                val indexFindProduct = matchFindProduct?.range?.last

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
                } else if (indexOpenProduct != null) {
                    val str = text.substring(indexOpenProduct + 1)
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
                            _navigateToProductDetails.value = num
                        else
                            _message.value = "You do not have an access to this product"
                    } else
                        _message.value = "Product list is empty"
                } else if (indexFindProduct != null) {
                    val str = recordedText.substring(indexFindProduct + 1)
                    retrieveList(str)
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    /**
     * method that converts text to number
     */
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

    /**
     * method that retrieves the product list
     */
    fun retrieveList(str: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            _products.value = getListWithString("%$str%", businessId)
        }
    }

    /**
     * method that sets a new value - id of the selected product
     */
    fun onProductClicked(id: Int) {
        _navigateToProductDetails.value = id
    }

    /**
     * method that sets the values of LiveData to null except for the product list
     */
    fun onProductNavigated() {
        _navigateToCreateNewProduct.value = null
        _navigateToProductDetails.value = null
        _message.value = null
        _closeFragment.value = null
    }

    /**
     * Suspending method that retrieves a list of the products
     */
    private suspend fun getAllProducts(businessId: Int): List<Product> {
        return withContext(Dispatchers.IO) {
            database.getAllProductsWithBusinessId(businessId)
        }
    }

    /**
     * Suspending method that retrieves a list of the products that contain line substring in their name
     */
    private suspend fun getListWithString(line: String, businessId: Int): List<Product> {
        return withContext(Dispatchers.IO) {
            database.getAllProductsWithString(line, businessId)
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