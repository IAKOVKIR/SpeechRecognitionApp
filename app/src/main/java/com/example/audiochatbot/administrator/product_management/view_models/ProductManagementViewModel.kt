package com.example.audiochatbot.administrator.product_management.view_models

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

    private val _navigateToProductDetails = MutableLiveData<Int>()
    val navigateToProductDetails
        get() = _navigateToProductDetails

    init {
        uiScope.launch {
            _products.value = getAllProducts(businessId)
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
        _navigateToProductDetails.value = null
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