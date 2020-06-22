package com.example.audiochatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.*
import kotlinx.coroutines.*

class LoginViewModel(private val userDatabase: UserDao, private val deliveryUserDatabase: DeliveryUserDao): ViewModel() {

    private var viewModelJob = Job()
    private var total: Int = 0

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _user = MutableLiveData<User?>()
    private var _deliveryUser = MutableLiveData<DeliveryUser?>()

    val user: LiveData<User?> get() = _user
    val deliveryUser: LiveData<DeliveryUser?> get() = _deliveryUser

    private fun getTotal() {
        uiScope.launch {
            total = getTotalUsers()
        }
    }

    private suspend fun getTotalUsers(): Int {
        return withContext(Dispatchers.IO) {
            userDatabase.getTotal()
        }
    }

    fun checkUser(userId: String, password: String) {
        uiScope.launch {
            if (userId.length > 1 && password.length > 7) {
                val id: Int = userId.substring(1).toInt()
                val char: Char = userId[0]

                if (char == 'D')
                    _deliveryUser.value = getDeliveryUser(id, password)
                else if (char == 'A' || char == 'E')
                    _user.value = getUser(id, password, char)
                else
                    _user.value = null
            } else {
                _user.value = null
            }
        }
    }

    private suspend fun getUser(userId: Int, password: String, char: Char): User? {
        return withContext(Dispatchers.IO) {
            userDatabase.getUser(userId, password, char)
        }
    }

    private suspend fun getDeliveryUser(userId: Int, password: String): DeliveryUser? {
        return withContext(Dispatchers.IO) {
            deliveryUserDatabase.getDeliveryUser(userId, password)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}