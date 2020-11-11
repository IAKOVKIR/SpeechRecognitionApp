package com.example.audiochatbot.administrator.product_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModel
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for CreateProductFragment.
 *
 * @param dataSource - UserDao reference.
 */
class CreateProductViewModel(
    private val dataSource: UserDao
) : ViewModel() {

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
     * a [CreateUserViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

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
            else if (text.contains("submit the details") || text.contains("submit") || text.contains("submit details"))
                _action.value = 1
            else
                _message.value = "Cannot understand your command"
        }
    }

    /**
     * method that creates a new product record
     */
    fun submitProduct(product: Product) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val result = validate(product)
            if (result) {
                val uLast = getLastProduct()
                product.productId = uLast!!.productId + 1
                addProductToDb(product)
                val u = getProductIdWithId(product.productId)
                if (u == 1)
                    _closeFragment.value = true
                else
                    _message.value = "Something went wrong!"
            }
        }
    }

    /**
     * Suspending method that inserts a new product
     */
    private suspend fun addProductToDb(product: Product) {
        withContext(Dispatchers.IO) {
            database.insertProduct(product)
        }
    }

    /**
     * Suspending method that retrieves the last Product
     */
    private suspend fun getLastProduct(): Product? {
        return withContext(Dispatchers.IO) {
            database.getLastProduct()
        }
    }

    /**
     * Suspending method that retrieves the product with Id
     */
    private suspend fun getProductIdWithId(productId: Int): Int {
        return withContext(Dispatchers.IO) {
            database.getProductIdWithId(productId)
        }
    }

    /**
     * method that validates the data entered in the fields
     */
    private fun validate(product: Product): Boolean {
        when {
            product.name.isEmpty() -> {
                _message.value = "The product name field is empty"
                return false
            }
            product.smallUnitName.isEmpty() -> {
                _message.value = "Small unit name field is empty"
                return false
            }
            product.bigUnitName.isEmpty() -> {
                _message.value = "Big unit name field is empty"
                return false
            }
            else -> {
                var fHalf = ""
                var sHalf = ""
                val conversion = product.conversion
                for (i in conversion.indices) {
                    if (conversion[i] == ':') {
                        fHalf = conversion.substring(0, i)
                        sHalf = conversion.substring(i + 1)
                        break
                    }
                }

                if (conversion == "") {
                    _message.value = "Conversion field is empty"
                    return false
                } else {
                    val num1: Int
                    val num2: Int
                    try {
                        num1 = fHalf.toInt()
                        num2 = sHalf.toInt()
                    } catch (e: NumberFormatException) {
                        _message.value = "Wrong number format is used in conversion field"
                        return false
                    }

                    if (num1 < 1 || num2 < 1) {
                        _message.value = "Negative numbers were entered in conversion field"
                        return false
                    }
                }

            }
        }
        return true
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
