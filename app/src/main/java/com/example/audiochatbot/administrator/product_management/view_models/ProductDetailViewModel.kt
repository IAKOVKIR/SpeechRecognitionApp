package com.example.audiochatbot.administrator.product_management.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModel
import com.example.audiochatbot.database.AssignedProduct
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for ProductDetailFragment.
 *
 * @param productKey The key of the current product we are working on.
 */
class ProductDetailViewModel(
    private val productKey: Int,
    private val storeKey: Int,
    val dataSource: UserDao
) : ViewModel() {

    /**
     * Hold a reference to SleepDatabase via its SleepDatabaseDao.
     */
    private val database = dataSource

    /** Coroutine setup variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = Job()

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [CreateUserViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _product = MutableLiveData<Product>()
    val product: LiveData<Product> get() = _product

    private var _assignedProduct = MutableLiveData<AssignedProduct>()
    val assignedProduct: LiveData<AssignedProduct> get() = _assignedProduct

    private val _isUploaded = MutableLiveData<Boolean>()
    val isUploaded
        get() = _isUploaded

    init {
        uiScope.launch {
            _product.value = retrieveProduct(productKey)
            _assignedProduct.value = retrieveAssignedProduct(productKey, storeKey)
        }
    }

    fun updateProduct(newProduct: Product) {
        newProduct.productId = product.value!!.productId
        newProduct.businessId = product.value!!.businessId
        submitProduct(newProduct)
    }

    fun updateProduct(newProduct: Product, sale: Int, quantity: Int) {
        newProduct.productId = product.value!!.productId
        newProduct.businessId = product.value!!.businessId
        submitProduct(newProduct)
        updateAssignedProduct(sale, quantity)
    }

    private fun submitProduct(product: Product) {
        uiScope.launch {
            addProductToDb(product)
            val u = retrieveProduct(product.productId)
            _isUploaded.value = u!!.productId == productKey
        }
    }

    private fun updateAssignedProduct(sale: Int, quantity: Int) {
        uiScope.launch {
            val newAssignedProduct: AssignedProduct = assignedProduct.value!!
            newAssignedProduct.sale = sale
            newAssignedProduct.quantity = quantity
            updateAProduct(newAssignedProduct)
        }
    }

    fun deleteRecord() {
        uiScope.launch {
            deleteRecordDb()
            val u = retrieveProduct(productKey)
            _isUploaded.value = u == null
        }
    }

    private suspend fun addProductToDb(product: Product) {
        withContext(Dispatchers.IO) {
            database.update(product)
        }
    }

    private suspend fun updateAProduct(assignedProduct: AssignedProduct) {
        withContext(Dispatchers.IO) {
            database.updateAssignedProduct(assignedProduct)
        }
    }

    private suspend fun retrieveProduct(productId: Int): Product? {
        return withContext(Dispatchers.IO) {
            database.getProductWithId(productId)
        }
    }

    private suspend fun retrieveAssignedProduct(productId: Int, storeId: Int): AssignedProduct? {
        return withContext(Dispatchers.IO) {
            database.getAssignedProduct(productId, storeId)
        }
    }

    private suspend fun deleteRecordDb() {
        withContext(Dispatchers.IO) {
            database.deleteProductRecord(productKey)
        }
    }

    /**
     * Cancels all coroutines when the ViewModel is cleared, to cleanup any pending work.
     *
     * onCleared() gets called when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}