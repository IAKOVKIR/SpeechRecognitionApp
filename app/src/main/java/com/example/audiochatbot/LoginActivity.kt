package com.example.audiochatbot

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.administrator.AdministratorActivity
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.daos.UserDao
import com.example.audiochatbot.databinding.ActivityLoginBinding
import com.example.audiochatbot.delivery_user.DeliveryUserActivity
import com.example.audiochatbot.employee.EmployeeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class LoginActivity : AppCompatActivity() {

    private lateinit var userDataSource: UserDao
    private lateinit var loginViewModel: LoginViewModel
    private var display = false

    override fun onStart() {
        super.onStart()
        getUser()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        val application = requireNotNull(this).application

        userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory = LoginViewModelFactory(userDataSource)

        loginViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(LoginViewModel::class.java)

        binding.button.setOnClickListener {
            val userId: String = binding.userId.text.toString().trim()
            val password: String = binding.password.text.toString().trim()
            loginViewModel.checkUser(userId, password)
        }

        loginViewModel.user.observe(this, Observer { user ->
            if (user != null) {
                if (user.position == 'E') {
                    rememberMe(user)
                    val intent = Intent(this, EmployeeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else if (user.position == 'A') {
                    rememberMe(user)
                    val intent = Intent(this, AdministratorActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else if (display) {
                Toast.makeText(applicationContext, "wrong user id or password", Toast.LENGTH_SHORT)
                    .show()
            }
            display = true
        })

        loginViewModel.deliveryUser.observe(this, Observer { user ->
            if (user != null) {
                val intent = Intent(this, DeliveryUserActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun getUser() {
        val pref: SharedPreferences = getSharedPreferences("eaPreferences", Context.MODE_PRIVATE)
        val id: Int = pref.getInt("id", 0)
        val position: String = pref.getString("position", "")!!.trim()
        val password: String = pref.getString("password", "")!!.trim()
        Log.e("res", "$position$id")
        loginViewModel.checkUser("$position$id", password)
    }

    private fun rememberMe(user: User) {
        getSharedPreferences("eaPreferences", Context.MODE_PRIVATE)
            .edit()
            .putInt("id", user.userId)
            .putInt("businessId", user.businessId)
            .putString("password", user.password)
            .putString("position", "${user.position}")
            .apply()
    }
}