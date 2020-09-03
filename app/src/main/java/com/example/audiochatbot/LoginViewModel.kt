package com.example.audiochatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.*
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*

class LoginViewModel(private val userDatabase: UserDao): ViewModel() {

    private var viewModelJob = Job()
    private var one: Int = 0

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _user = MutableLiveData<User?>()

    val user: LiveData<User?> get() = _user

    init {
        getTotal()
    }

    private fun getTotal() {
        uiScope.launch {
            one = getNumber()
        }
    }

    private suspend fun getNumber(): Int {
        return withContext(Dispatchers.IO) {
            userDatabase.getOne()
        }
    }

    fun checkUser(userId: String, password: String) {
        uiScope.launch {
            if (userId.length > 1 && password.length > 7) {
                val id: Int = userId.substring(1).toInt()
                val char: Char = userId[0]

                if (char == 'A' || char == 'E' || char == 'D')
                    _user.value = getUser(id, password, char)
                else
                    _user.value = null
            } else
                _user.value = null
        }
    }

    private suspend fun getUser(userId: Int, password: String, char: Char): User? {
        return withContext(Dispatchers.IO) {
            userDatabase.getUser(userId, password, char)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}