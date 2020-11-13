package com.example.audiochatbot.delivery_user.delivery_user_home.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel for DeliveryUserHomeFragment.
 */
class DeliveryUserHomeViewModel: ViewModel() {

    /**
     * String arrays that contain all the available commands for this view.
     */
    private val deliveryListArray = arrayOf("open the delivery list", "delivery list", "open delivery list", "open the delivery least",
        "delivery least", "open delivery least", "deliver released")
    private val logOutArray = arrayOf("log out", "logout")

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
     * a [DeliveryUserHomeViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Lifecycle-aware observable that stores the value of the action
     */
    private var _action = MutableLiveData<Int>()
    val action: LiveData<Int> get() = _action

    /**
     * method that checks a given string with all the available ones and then sets a new value for the action
     */
    @SuppressLint("DefaultLocale")
    fun convertStringToAction(text: String) {
        //launch a new coroutine in background and continue
        uiScope.launch {
            val newText = text.toLowerCase()
            //convert string to action
            when {
                deliveryListArray.contains(newText) -> _action.value = 1
                logOutArray.contains(newText) -> _action.value = 2
                else -> _action.value = 0
            }
        }
    }

    /**
     * Sets a new value of the action
     */
    fun setAction(num: Int) {
        _action.value = num
    }

    /**
     * Sets the value of action as -1
     */
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