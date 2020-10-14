package com.example.audiochatbot.administrator.store_management.view_models

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class AssignedUsersViewModel(val storeId: Int, private val database: UserDao): ViewModel() {

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
     * a [AssignedUsersViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val users = database.getAllUsersLiveWithStoreIDNoAdmins(storeId)

    private val _navigateToUserDetails = MutableLiveData<Int>()
    val navigateToUserDetails
        get() = _navigateToUserDetails

    private val _navigateToAssignUsers = MutableLiveData<Boolean>()
    val navigateToAssignUsers
        get() = _navigateToAssignUsers

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?>
        get() = _message

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(recordedText: String) {
        uiScope.launch {
            val text = recordedText.toLowerCase()
            Log.e("heh", text)
            if (text.contains("go back"))
                _closeFragment.value = true
            else if (text.contains("assign users"))
                _navigateToAssignUsers.value = true
            else {
                val matchAddProductNumber = "remove user number".toRegex().find(text)
                val indexAddProductNumber = matchAddProductNumber?.range?.last

                if (indexAddProductNumber != null) {
                    val num = textToInteger(text, indexAddProductNumber)

                    if (num > 0) {
                        val list = users.value
                        var res = false

                        if (list != null) {
                            for (i in list) {
                                if (i.userId == num) {
                                    res = true
                                    break
                                }
                            }

                            if (res)
                                deleteRecord(num)
                            else
                                _message.value = "You do not have an access to this product"
                        } else
                            _message.value = "Product list is empty"
                    } else
                        _message.value = "Cannot understand your command"
                } else
                    _message.value = "Cannot understand your command"
            }
        }
    }

    private fun textToInteger(text: String, lastIndex: Int): Int {
        val str = text.substring(lastIndex + 1)
        val result = str.filter { it.isDigit() }

        return when {
            result != "" -> {
                Log.e("heh", result)
                result.toInt()
            }
            str.contains("one") -> 1
            str.contains("to") || str.contains("two") -> 2
            str.contains("three") -> 3
            str.contains("for") -> 4
            else -> -1
        }
    }

    fun onUserClicked(id: Int) {
        _navigateToUserDetails.value = id
    }

    fun onUserNavigated() {
        _navigateToUserDetails.value = null
        _navigateToAssignUsers.value = null
        _message.value = null
        _closeFragment.value = null
    }

    fun deleteRecord(userId: Int) {
        uiScope.launch {
            deleteRecordDb(userId)
        }
    }

    private suspend fun deleteRecordDb(userId: Int) {
        withContext(Dispatchers.IO) {
            database.removeUserFromStore(userId, storeId)
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