package com.example.audiochatbot.administrator.product_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModel
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class CreateProductViewModel(
    private val database: UserDao
) : ViewModel() {
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
     * a [CreateUserViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _isUploaded = MutableLiveData<Boolean>()
    val isUploaded
        get() = _isUploaded

    fun submitProduct(product: Product, adminId: Int) {
        uiScope.launch {
            val uLast = getLastProduct()
            product.productId = uLast!!.productId + 1
            product.businessId = getAdminBusinessId(adminId)
            addProductToDb(product)
            val u = getProductIdWithId(product.productId)
            _isUploaded.value = u == 1
        }
    }

    private suspend fun addProductToDb(product: Product) {
        withContext(Dispatchers.IO) {
            database.insertProduct(product)
        }
    }

    private suspend fun getLastProduct(): Product? {
        return withContext(Dispatchers.IO) {
            database.getLastProduct()
        }
    }

    private suspend fun getProductIdWithId(productId: Int): Int {
        return withContext(Dispatchers.IO) {
            database.getProductIdWithId(productId)
        }
    }

    private suspend fun getAdminBusinessId(adminId: Int): Int {
        return withContext(Dispatchers.IO) {
            database.getAdminsBusinessId(adminId)
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
