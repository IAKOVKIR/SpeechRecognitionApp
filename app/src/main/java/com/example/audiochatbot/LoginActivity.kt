package com.example.audiochatbot

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.administrator.AdministratorActivity
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.database.User
import com.example.audiochatbot.databinding.ActivityLoginBinding
import com.example.audiochatbot.delivery_user.DeliveryUserActivity
import com.example.audiochatbot.employee.EmployeeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        val application = requireNotNull(this).application

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        val deliveryUserDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).deliveryUserDao

        val viewModelFactory = LoginViewModelFactory(userDataSource, deliveryUserDataSource)

        val loginViewModel =
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
            } else
                Toast.makeText(applicationContext, "wrong user id or password", Toast.LENGTH_SHORT).show()
        })

        loginViewModel.deliveryUser.observe(this, Observer { user ->
            if (user != null) {
                val intent = Intent(this, DeliveryUserActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun rememberMe(user: User) {
        getSharedPreferences("eaPreferences", Context.MODE_PRIVATE)
            .edit()
            .putInt("id", user.userId)
            .putString("password", user.password)
            .putString("position", "${user.position}")
            .apply()
    }
}