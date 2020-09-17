package com.example.audiochatbot.administrator.user_management.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class CreateUserViewModel(val dataSource: UserDao) : ViewModel() {
    private val positionCharArray = arrayOf('E', 'A', 'D')

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

    private var position = positionCharArray[0]

    private val _isUploaded = MutableLiveData<Boolean>()
    val isUploaded
        get() = _isUploaded

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    fun setPos(pos: Int) {
        position = positionCharArray[pos]
    }

    fun submitUser(firstName: String, lastName: String, email: String, phoneNumber: String,
                   password: String, adminId: Int) {
        uiScope.launch {
            val userId = getLastUserId()
            val businessId = getAdminBusinessId(adminId)

            if (firstName.isNotEmpty()) {
                if (lastName.isNotEmpty()) {
                    if (email.isNotEmpty()) {
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            if (checkPhone(phoneNumber)) {
                                if (password.length > 7) {
                                    val newUser = User(
                                        userId, businessId,
                                        firstName, lastName,
                                        email, phoneNumber,
                                        password, position)

                                    addUserToDb(newUser)

                                    _isUploaded.value = getUser(userId) != null
                                } else
                                    _errorMessage.value = "password's length is less than 8 symbols"
                            } else
                                _errorMessage.value = "wrong phone format"
                        } else
                            _errorMessage.value = "wrong email format"
                    } else
                        _errorMessage.value = "email field is empty"
                } else
                    _errorMessage.value = "Last name field is empty"
            } else
                _errorMessage.value = "First name field is empty"
        }
    }

    private suspend fun addUserToDb(user: User) {
        withContext(Dispatchers.IO) {
            database.insertUser(user)
        }
    }

    private suspend fun getLastUserId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastUserId()
        }
    }

    private suspend fun getUser(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userId)
        }
    }

    private suspend fun getAdminBusinessId(adminId: Int): Int {
        return withContext(Dispatchers.IO) {
            database.getAdminsBusinessId(adminId)
        }
    }

    private fun checkPhone(d: String): Boolean {
        val pattern: Pattern = Pattern.compile("^\\d{10}$")
        val matcher: Matcher = pattern.matcher(d)
        return matcher.matches()
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