package com.example.audiochatbot

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class LoginActivity : AppCompatActivity() {

    private lateinit var userDataSource: UserDao
    private lateinit var loginViewModel: LoginViewModel
    private var displayMessage: Boolean = false

    override fun onStart() {
        super.onStart()
        //reads username and password from SharedPreferences
        getUser()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        val deliveryUserDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).deliveryUserDao

        val viewModelFactory = LoginViewModelFactory(userDataSource, deliveryUserDataSource)

        loginViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(LoginViewModel::class.java)

        //reads username and password from SharedPreferences
        //getUser()
        binding.button.setOnClickListener {
            val userId: String = binding.userId.text.toString().trim()
            val password: String = binding.password.text.toString().trim()
            displayMessage = true
            loginViewModel.checkUser(userId, password)
        }

        loginViewModel.user.observe(this, Observer {user ->
            if (user != null) {
                rememberMe(user)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else
                Toast.makeText(applicationContext, "wrong user id or password", Toast.LENGTH_SHORT).show()
        })

    }

    /**
     * @function getUser() checks username and password.
     * If there is an account with same username and password, then UserHomeActivity activity will be opened directly
     */

    private fun getUser() {
        val pref: SharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val id: Int = pref.getInt("id", -1)
        val password: String = pref.getString("password", "") as String
        val position: String = pref.getString("position", "") as String

        loginViewModel.checkUser("$position$id", password)
    }

    /**
     * @function rememberMe() saves username, password and other values in SharedPreferences
     */

    //rememberMe() function saves id, phone number and password in SharedPreferences
    private fun rememberMe(user: User) {
        getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            .edit()
            .putInt("id", user.userId)
            .putString("password", user.password)
            .putString("position", "${user.position}")
            .apply()
    }
}