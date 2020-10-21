package com.example.audiochatbot.delivery_user.delivery_user_home.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeliveryUserHomeViewModel: ViewModel() {
    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()
    private val deliveryListArray = arrayOf("open the delivery list", "delivery list")
    private val logOutArray = arrayOf("log out", "logout")

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [DeliveryUserHomeViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _action = MutableLiveData<Int>()
    val action: LiveData<Int> get() = _action

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(text: String) {
        uiScope.launch {
            val newText = text.toLowerCase()
            when {
                deliveryListArray.contains(newText) -> _action.value = 1
                logOutArray.contains(newText) -> _action.value = 2
                else -> _action.value = 0
            }
        }
    }

    fun setAction(num: Int) {
        _action.value = num
    }

    fun cancelAction() {
        _action.value = -1
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