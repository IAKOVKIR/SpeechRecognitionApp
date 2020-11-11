package com.example.audiochatbot.administrator.product_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.AssignedProduct
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for ProductDetailFragment.
 *
 * @param productId - the key of the current product we are working on.
 * @param storeId - the key of the current store we are working on.
 * @param dataSource - UserDao reference.
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

    /**
     * Lifecycle-aware observable that stores the Product value
     */
    private var _product = MutableLiveData<Product>()
    val product: LiveData<Product> get() = _product

    /**
     * Lifecycle-aware observable that stores the AssignedProduct value
     */
    private var _assignedProduct = MutableLiveData<AssignedProduct>()
    val assignedProduct: LiveData<AssignedProduct> get() = _assignedProduct

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    /**
     * Lifecycle-aware observable that stores the Boolean value
     */
    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    /**
     * Lifecycle-aware observable that stores the Int value
     */
    private val _action = MutableLiveData<Int>()
    val action get() = _action

    init {
        //launch a new coroutine in background and continue
        uiScope.launch {
            //prepopulate the LiveData
            _product.value = retrieveProduct(productId)
            _assignedProduct.value = retrieveAssignedProduct(productId, storeId)
        }
    }

    /**
     * method that checks a given string with all the available ones and then chooses the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val text = givenText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("delete the product") || text.contains("delete product") || text.contains("delete this product"))
                deleteRecord()
            else if (text.contains("update the details") || text.contains("update") || text.contains("update details"))
                _action.value = 1
            else
                _errorMessage.value = "Cannot understand your command"
        }
    }

    /**
     * method that validates the product
     */
     fun submitProduct(name: String, smallUnitName: String, bigUnitName: String, conversion: String, price: Float) {
         //launch a new coroutine in background and continue
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
                                if (u == newProduct)
                                    _closeFragment.value = true
                                else
                                    _errorMessage.value = "Something went wrong!"
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

    /**
     * method that updates the product
     */
    fun updateProduct(name: String, smallUnitName: String, bigUnitName: String, conversion: String,
                      price: Float, sale: Int, quantity: Int) {
        submitProduct(name, smallUnitName, bigUnitName, conversion, price)
        updateAssignedProduct(sale, quantity)
    }

    /**
     * method that the AssignedProduct
     */
    private fun updateAssignedProduct(sale: Int, quantity: Int) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val newAssignedProduct = retrieveAssignedProduct(productId, storeId)
            newAssignedProduct!!.sale = sale
            newAssignedProduct.quantity = quantity
            updateAProduct(newAssignedProduct)
        }
    }

    /**
     * method that deletes the product
     */
    fun deleteRecord() {
        //launch a new coroutine in background and continue
        uiScope.launch {
            deleteRecordDb()
            val u = retrieveProduct(productId)
            if (u == null)
                _closeFragment.value = true
            else
                _errorMessage.value = "Something went wrong!"
        }
    }

    /**
     * Suspending method that updates the product record
     */
    private suspend fun addProductToDb(product: Product) {
        withContext(Dispatchers.IO) {
            database.update(product)
        }
    }

    /**
     * Suspending method that updates the AssignedProduct record
     */
    private suspend fun updateAProduct(assignedProduct: AssignedProduct) {
        withContext(Dispatchers.IO) {
            database.updateAssignedProduct(assignedProduct)
        }
    }

    /**
     * Suspending method that retrieves the product with product Id
     */
    private suspend fun retrieveProduct(productId: Int): Product? {
        return withContext(Dispatchers.IO) {
            database.getProductWithId(productId)
        }
    }

    /**
     * Suspending method that retrieves the AssignedProduct with product and store ids
     */
    private suspend fun retrieveAssignedProduct(productId: Int, storeId: Int): AssignedProduct? {
        return withContext(Dispatchers.IO) {
            database.getAssignedProduct(productId, storeId)
        }
    }

    /**
     * Suspending method that deletes the record
     */
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