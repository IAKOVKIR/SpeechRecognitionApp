package com.example.audiochatbot

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.administrator.AdministratorActivity
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.database.models.User
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.ActivityLoginBinding
import com.example.audiochatbot.delivery_user.DeliveryUserActivity
import com.example.audiochatbot.employee.EmployeeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*


class LoginActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var userDataSource: UserDao
    private lateinit var loginViewModel: LoginViewModel
    private var display = false
    private var textToSpeech: TextToSpeech? = null

    override fun onStart() {
        super.onStart()
        getUser()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_login)

        // Get the AudioManager service
        val audio = getSystemService(AUDIO_SERVICE) as AudioManager

        val application = requireNotNull(this).application

        userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory = LoginViewModelFactory(userDataSource)

        textToSpeech = TextToSpeech(applicationContext, this)

        loginViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(LoginViewModel::class.java)

        binding.button.setOnClickListener {
            val userId: String = binding.userId.text.toString().trim()
            val password: String = binding.password.text.toString().trim()
            loginViewModel.checkUser(userId, password)
        }

        loginViewModel.user.observe(this, { user ->
            if (user != null) {
                when (user.position) {
                    'E' -> {
                        rememberMe(user)
                        val intent = Intent(this, EmployeeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    'A' -> {
                        rememberMe(user)
                        val intent = Intent(this, AdministratorActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    'D' -> {
                        rememberMe(user)
                        val intent = Intent(this, DeliveryUserActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else if (display) {
                // 0 - 15 are usually available on any device
                val musicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

                if (musicVolume == 0)
                    Toast.makeText(applicationContext, "wrong user id or password", Toast.LENGTH_SHORT).show()
                else
                    textToSpeech!!.speak(
                        "wrong user id or password",
                        TextToSpeech.QUEUE_FLUSH, null, null)
            }
            display = true
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set UK English as language for tts
            val result = textToSpeech!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.e("TTS", "The Language specified is not supported!")
        } else
            Log.e("TTS", "Initialization Failed!")
    }

    private fun getUser() {
        val pref: SharedPreferences = getSharedPreferences("eaPreferences", Context.MODE_PRIVATE)
        val id: Int = pref.getInt("id", 0)
        val position: String = pref.getString("position", "")!!.trim()
        val password: String = pref.getString("password", "")!!.trim()
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

    override fun onStop() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }

        super.onStop()
    }

    override fun onDestroy() {
        // Shut down TTS
        if (textToSpeech != null) {
            textToSpeech!!.shutdown()
        }

        super.onDestroy()
    }
}