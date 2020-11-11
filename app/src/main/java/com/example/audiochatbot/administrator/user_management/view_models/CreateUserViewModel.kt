package com.example.audiochatbot.administrator.user_management.view_models

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.models.User
import com.example.audiochatbot.database.UserDao
import kotlinx.coroutines.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * ViewModel for CreateUserFragment.
 *
 * @param dataSource - UserDao reference.
 */
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

    /**
     * Lifecycle-aware observable that stores the String value
     */
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

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
     * method that sets the position
     */
    fun setPos(pos: Int) {
        position = positionCharArray[pos]
    }

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
                _errorMessage.value = "Cannot understand your command"
        }
    }

    /**
     * Suspending method that submits the user details
     */
    fun submitUser(firstName: String, lastName: String, email: String, phoneNumber: String,
                   password: String) {

        //launch a new coroutine in background and continue
        uiScope.launch {
            val userId = getLastUserId() + 1

            if (firstName.isNotEmpty()) {
                if (lastName.isNotEmpty()) {
                    if (email.isNotEmpty()) {
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            if (checkPhone(phoneNumber)) {
                                if (password.length > 7) {
                                    val newUser = User(
                                        userId, 1,
                                        firstName, lastName,
                                        email, phoneNumber,
                                        password, position)

                                    addUserToDb(newUser)

                                    if (getUser(userId) != null) {
                                        _closeFragment.value = true
                                    } else {
                                        _errorMessage.value = "Something went wrong"
                                    }
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

    /**
     * Suspending method that inserts a new user record
     */
    private suspend fun addUserToDb(user: User) {
        withContext(Dispatchers.IO) {
            database.insertUser(user)
        }
    }

    /**
     * Suspending method that retrieves the last user id
     */
    private suspend fun getLastUserId(): Int {
        return withContext(Dispatchers.IO) {
            database.getLastUserId()
        }
    }

    /**
     * Suspending method that retrieves a user record
     */
    private suspend fun getUser(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            database.getUserWithId(userId)
        }
    }

    /**
     * method that validates the phone
     */
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