package com.example.audiochatbot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.database.User
import com.example.audiochatbot.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

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

        loginViewModel.user.observe(this, Observer {user ->
            if (user == null) {
                Toast.makeText(applicationContext, "wrong user id or password", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        })

    }
}