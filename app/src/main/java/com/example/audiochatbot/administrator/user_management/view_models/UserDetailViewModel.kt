package com.example.audiochatbot.administrator.user_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * ViewModel for UserDetailFragment.
 *
 * @param userId The key of the current user we are working on.
 */
class UserDetailViewModel(
    private val userId: Int,
    val dataSource: UserDao
) : ViewModel() {
    private val positionCharArray = arrayOf('E', 'A', 'D')

    /**
     * Hold a reference to SleepDatabase via its SleepDatabaseDao.
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
     * a [UserDetailViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private var position = 'E'

    private val _closeFragment = MutableLiveData<Boolean>()
    val closeFragment get() = _closeFragment

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    private val _action = MutableLiveData<Int>()
    val action get() = _action

    init {
        uiScope.launch {
            _user.value = retrieveUser(userId)
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertStringToAction(givenText: String) {
        uiScope.launch {
            val text = givenText.toLowerCase()
            if (text.contains("go back") || text.contains("return back"))
                _closeFragment.value = true
            else if (text.contains("delete the user") || text.contains("delete user") || text.contains("delete this user"))
                deleteRecord()
            else if (text.contains("update the details") || text.contains("update") || text.contains("update details"))
                _action.value = 1
            else
                _errorMessage.value = "Cannot understand your command"
        }
    }

    fun updateUser(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String) {
        uiScope.launch {
            if (password == repeatPassword) {
                if (firstName.isNotEmpty()) {
                    if (lastName.isNotEmpty()) {
                        if (email.isNotEmpty()) {
                            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                if (checkPhone(phoneNumber)) {
                                    if (password.length > 7) {
                                        val updatedUser = User(
                                            userId,
                                            user.value!!.businessId,
                                            firstName,
                                            lastName,
                                            email,
                                            phoneNumber,
                                            password,
                                            position
                                        )
                                        submitUser(updatedUser)
                                    } else
                                        _errorMessage.value =
                                            "password's length is less than 8 symbols"
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
            } else
                _errorMessage.value = "Passwords do not match"
        }
    }

    private fun submitUser(user: User) {
        uiScope.launch {
            addUserToDb(user)
            val u = retrieveUser(user.userId)
            if (u!!.userId == user.userId) {
                _closeFragment.value = true
            } else
                _errorMessage.value = "Something went wrong"
        }
    }

    fun deleteRecord() {
        uiScope.launch {
            deleteRecordDb()
            val u = retrieveUser(userId)
            if (u == null) {
                _closeFragment.value = true
            } else
                _errorMessage.value = "Something went wrong"
        }
    }

    private suspend fun retrieveUser(userKey: Int): User? {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userKey)
        }
    }

    private suspend fun addUserToDb(user: User) {
        withContext(Dispatchers.IO) {
            database.update(user)
        }
    }

    private suspend fun deleteRecordDb() {
        withContext(Dispatchers.IO) {
            database.deleteUserRecord(userId)
        }
    }

    private fun checkPhone(d: String): Boolean {
        val pattern: Pattern = Pattern.compile("^\\d{10}$")
        val matcher: Matcher = pattern.matcher(d)
        return matcher.matches()
    }

    fun setPos(pos: Int) {
        position = positionCharArray[pos]
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

