package com.example.audiochatbot.administrator.product_management.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.AssignedProduct
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for ProductDetailFragment.
 *
 * @param productId - the key of the current product we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param dataSource -
 */
class ProductDetailViewModel(
    private val productId: Int,
    private val storeId: Int,
    val dataSource: UserDao
) : ViewModel() {

    /**
     * Hold a reference to UniDatabase via its UserDao.
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
     * a [ProductDetailViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _product = MutableLiveData<Product>()
    val product: LiveData<Product> get() = _product

    private var _assignedProduct = MutableLiveData<AssignedProduct>()
    val assignedProduct: LiveData<AssignedProduct> get() = _assignedProduct

    private val _isUploaded = MutableLiveData<Boolean>()
    val isUploaded
        get() = _isUploaded

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    init {
        uiScope.launch {
            _product.value = retrieveProduct(productId)
        }
    }

    fun setMessage(message: String) {
        _errorMessage.value = message
    }

     fun submitProduct(name: String, smallUnitName: String, bigUnitName: String, conversion: String, price: Float) {
        uiScope.launch {
            if (name.isNotEmpty()) {
                if (smallUnitName.isNotEmpty()) {
                    if (bigUnitName.isNotEmpty()) {
                        if (conversion.isNotEmpty()) {
                            if (price > 0) {
                                val newProduct = Product(productId, product.value!!.businessId,
                                    name, smallUnitName, bigUnitName, conversion, price)
                                addProductToDb(newProduct)
                                val u = retrieveProduct(productId)
                                _isUploaded.value = u == newProduct
                            } else
                                _errorMessage.value = "price value has to be higher than zero"
                        } else
                            _errorMessage.value = "conversion field is empty"
                    } else
                        _errorMessage.value = "big unit name is empty"
                } else
                    _errorMessage.value = "small unit name is empty"
            } else
                _errorMessage.value = "Name field is empty"
        }
    }

    fun updateProduct(name: String, smallUnitName: String, bigUnitName: String, conversion: String,
                      price: Float, sale: Int, quantity: Int) {
        submitProduct(name, smallUnitName, bigUnitName, conversion, price)
        updateAssignedProduct(sale, quantity)
    }

    private fun updateAssignedProduct(sale: Int, quantity: Int) {
        uiScope.launch {
            val newAssignedProduct = retrieveAssignedProduct(productId, storeId)
            newAssignedProduct!!.sale = sale
            newAssignedProduct.quantity = quantity
            updateAProduct(newAssignedProduct)
        }
    }

    fun deleteRecord() {
        uiScope.launch {
            deleteRecordDb()
            val u = retrieveProduct(productId)
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
            database.deleteProductRecord(productId)
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