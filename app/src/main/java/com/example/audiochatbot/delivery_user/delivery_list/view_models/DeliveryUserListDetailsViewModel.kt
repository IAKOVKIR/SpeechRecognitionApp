package com.example.audiochatbot.delivery_user.delivery_list.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.DeliveryProduct
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

/**
 * ViewModel for DeliveryUserListDetailsFragment.
 *
 * @param deliveryId - the key of the current delivery we are working on.
 * @param dataSource - the reference to UniDatabase
 */
class DeliveryUserListDetailsViewModel(val deliveryId: Int, val dataSource: UserDao): ViewModel() {

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
     * a [DeliveryUserListDetailsViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _deliveryProducts = MutableLiveData<List<DeliveryProduct>>()
    val deliveryProducts: LiveData<List<DeliveryProduct>> get() = _deliveryProducts

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    init {
        uiScope.launch {
            _deliveryProducts.value = getItems()
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(newText: String) {
        uiScope.launch {
            val text = newText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else
                _message.value = "Cannot recognise your command"

        }
    }

    private suspend fun getItems(): List<DeliveryProduct> {
        return withContext(Dispatchers.IO) {
            database.getAllDeliveryProducts(deliveryId)
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