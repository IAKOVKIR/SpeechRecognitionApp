package com.example.audiochatbot.delivery_user.delivery_user_home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.LoginActivity
import com.example.audiochatbot.R
import com.example.audiochatbot.databinding.FragmentDeliveryUserHomeBinding
import com.example.audiochatbot.delivery_user.delivery_user_home.view_models.DeliveryUserHomeViewModel
import java.util.*

/**
 * A [Fragment] subclass that represent the delivery user home view.
 */
class DeliveryUserHomeFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private lateinit var pref: SharedPreferences
    private lateinit var viewModel: DeliveryUserHomeViewModel
    private val requestCodeStt = 1
    private var response = false
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = requireActivity().getSharedPreferences("eaPreferences", Context.MODE_PRIVATE)
        // Performs the decision making of this fragment.
        viewModel = ViewModelProvider(this).get(DeliveryUserHomeViewModel::class.java)
        textToSpeech = TextToSpeech(requireActivity(), this)

        userId = pref.getInt("id", -1)
        // If the user id is equal to -1 then user activity is gonna be closed
        if (userId == -1)
            logOut()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding: FragmentDeliveryUserHomeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_delivery_user_home, container, false)

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        binding.apply {
            microphoneImage.setOnClickListener {
                // Get the Intent action
                val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                // Language model defines the purpose, there are special models for other use cases, like search.
                sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // Adding an extra language, you can use any language from the Locale class.
                sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                // Text that shows up on the Speech input prompt.
                sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "open the delivery list")
                try {
                    // Start the intent for a result, and pass in our request code.
                    startActivityForResult(sttIntent, requestCodeStt)
                } catch (e: ActivityNotFoundException) {
                    // Handling error when the service is not available.
                    e.printStackTrace()

                    Toast.makeText(requireContext(), "Your device does not support STT.", Toast.LENGTH_LONG).show()
                }
            }

            viewDeliveryListButton.setOnClickListener {
                viewModel.setAction(1)
            }

            logOutButton.setOnClickListener {
                viewModel.setAction(2)
            }
        }

        /**
         * Observe the LiveData, passing in this fragment as the LifecycleOwner and the observer.
         */
        viewModel.action.observe(viewLifecycleOwner, {
            when (it) {
                0 -> {
                    // 0 - 15 are usually available on any device
                    val musicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

                    // If the music volume is too quite then the toast message will be displayed
                    if (musicVolume < 1 || !response)
                        Toast.makeText(requireContext(), "Cannot understand your command", Toast.LENGTH_SHORT).show()
                    else
                        textToSpeech!!.speak("Cannot understand your command", TextToSpeech.QUEUE_FLUSH, null, null)
                }
                1 -> {
                    // Navigate to the DeliveryUserSelectStoreFragment and pass the user id
                    this.findNavController().navigate(
                        DeliveryUserHomeFragmentDirections.actionHomeAdministratorToDeliveryUserSelectStore(userId)
                    )
                    viewModel.cancelAction()
                }
                2 -> logOut()
            }
        })

        return binding.root
    }

    /**
     * @function [logOut] removes all data from SharedPreferences and starts Login activity
     * removing all the activities and fragments that were not destroyed before
     */
    private fun logOut() {
        val editor: SharedPreferences.Editor = pref.edit()
        editor.clear()
        editor.apply()

        val loginIntent = Intent(requireActivity(), LoginActivity::class.java)
        // set the new task and clear flags
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
        requireActivity().finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            requestCodeStt -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        // Recognized text is in the first position.
                        val recognizedText = result[0]
                        // Do what you want with the recognized text.
                        viewModel.convertStringToAction(recognizedText)
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        // If tts engine is configured
        if (status == TextToSpeech.SUCCESS) {
            // Set UK English as language for tts
            val result = textToSpeech!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.e("TTS", "The Language specified is not supported!")
            else
                response = true
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    override fun onStop() {
        // Stop TTS engine
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }

        super.onStop()
    }

    override fun onDestroy() {
        // Shut down TTS engine
        if (textToSpeech != null) {
            textToSpeech!!.shutdown()
        }

        super.onDestroy()
    }
}