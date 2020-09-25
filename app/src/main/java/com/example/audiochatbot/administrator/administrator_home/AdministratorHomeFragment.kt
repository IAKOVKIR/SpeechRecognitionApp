package com.example.audiochatbot.administrator.administrator_home

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.LoginActivity
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.administrator_home.view_models.AdministratorHomeViewModel
import com.example.audiochatbot.databinding.FragmentAdministratorHomeBinding
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
private const val REQUEST_CODE_STT = 1

class AdministratorHomeFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private lateinit var pref: SharedPreferences
    private lateinit var viewModel: AdministratorHomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = requireActivity().getSharedPreferences("eaPreferences", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(AdministratorHomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAdministratorHomeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_administrator_home, container, false)
        val adminId: Int = pref.getInt("id", -1)
        val businessId: Int = pref.getInt("businessId", -1)

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        if (adminId == -1)
            logOut()

        binding.microphoneImage.setOnClickListener {
            // Get the Intent action
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // Language model defines the purpose, there are special models for other use cases, like search.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Adding an extra language, you can use any language from the Locale class.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // Text that shows up on the Speech input prompt.
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
            try {
                // Start the intent for a result, and pass in our request code.
                startActivityForResult(sttIntent, REQUEST_CODE_STT)
            } catch (e: ActivityNotFoundException) {
                // Handling error when the service is not available.
                e.printStackTrace()

                Toast.makeText(requireContext(), "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }

        binding.apply {
            viewInventoryButton.setOnClickListener {
                viewModel.setAction(1)
            }

            viewDeliveryListButton.setOnClickListener {
                viewModel.setAction(2)
            }

            discardItemsButton.setOnClickListener {
                viewModel.setAction(3)
            }

            cashReportButton.setOnClickListener {
                viewModel.setAction(4)
            }

            userManagementButton.setOnClickListener {
                viewModel.setAction(5)
            }

            storeManagementButton.setOnClickListener {
                viewModel.setAction(6)
            }

            productManagementButton.setOnClickListener {
                viewModel.setAction(7)
            }

            logOutButton.setOnClickListener {
                viewModel.setAction(8)
            }
        }

        viewModel.action.observe(viewLifecycleOwner, {
            when (it) {
                0 -> {
                    // 0 - 15 are usually available on any device
                    val musicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

                    if (musicVolume == 0)
                        Toast.makeText(requireContext(), "Can't recognize your command", Toast.LENGTH_SHORT).show()
                    else
                        textToSpeech!!.speak("Can't recognize your command", TextToSpeech.QUEUE_FLUSH, null, null)
                }
                1 -> {
                    this.findNavController().navigate(
                        AdministratorHomeFragmentDirections.actionHomeAdministratorToDiscardItemStoreFragment(
                            adminId, businessId, 2)
                    )
                    viewModel.cancelAction()
                }
                2 -> {
                    this.findNavController().navigate(
                        AdministratorHomeFragmentDirections.actionHomeAdministratorToDiscardItemStoreFragment(
                            adminId, businessId, 1)
                    )
                    viewModel.cancelAction()
                }
                3 -> {
                    this.findNavController().navigate(
                        AdministratorHomeFragmentDirections.actionHomeAdministratorToDiscardItemStoreFragment(
                            adminId, businessId, 0)
                    )
                    viewModel.cancelAction()
                }
                4 -> {
                    this.findNavController().navigate(
                        AdministratorHomeFragmentDirections.actionHomeAdministratorToDiscardItemStoreFragment(
                            adminId, businessId, 3)
                    )
                    viewModel.cancelAction()
                }
                5 -> {
                    this.findNavController().navigate(
                        AdministratorHomeFragmentDirections.actionHomeDestinationToTestFragment(
                            adminId, businessId)
                    )
                    viewModel.cancelAction()
                }
                6 -> {
                    this.findNavController().navigate(
                        AdministratorHomeFragmentDirections.actionHomeDestinationToStoreManagementFragment(
                            adminId, businessId)
                    )
                    viewModel.cancelAction()
                }
                7 -> {
                    this.findNavController().navigate(
                        AdministratorHomeFragmentDirections.actionHomeAdministratorToProductManagementFragment(
                            adminId, businessId)
                    )
                    viewModel.cancelAction()
                }
                8 -> logOut()
            }
        })

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            REQUEST_CODE_STT -> {
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set UK English as language for tts
            val result = textToSpeech!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.e("TTS", "The Language specified is not supported!")
        } else
            Log.e("TTS", "Initialization Failed!")
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