package com.example.audiochatbot.administrator.user_management

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.UserDetailBinding
import com.example.audiochatbot.administrator.user_management.view_models.UserDetailViewModel
import com.example.audiochatbot.administrator.user_management.view_models.UserDetailViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class UserDetailFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var userDetailViewModel: UserDetailViewModel
    private var textToSpeech: TextToSpeech? = null
    private val requestCodeStt = 1
    private var response = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: UserDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.user_detail, container, false)

        // Create an instance of the Application
        val application = requireNotNull(this.activity).application

        // Get the AudioManager service
        val audio = activity?.getSystemService(AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        // Create an instance of the UserDetailFragmentArgs
        val arguments =
            UserDetailFragmentArgs.fromBundle(requireArguments())

        // Create an instance of the ViewModel Factory.
        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        val viewModelFactory = UserDetailViewModelFactory(arguments.userKey, dataSource)

        // Get a reference to the ViewModel associated with this fragment.
        userDetailViewModel =
            ViewModelProvider(
            this, viewModelFactory).get(UserDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.userDetailViewModel = userDetailViewModel
        binding.lifecycleOwner = this

        val adapter = ArrayAdapter(this.requireContext(),
            android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Positions))
        binding.spinner.adapter = adapter

        binding.apply {

            /**
             * Calls deleteRecord() method
             */
            deleteRecord.setOnClickListener {
                userDetailViewModel!!.deleteRecord()
            }

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    userDetailViewModel!!.setPos(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            /**
             * Retrieves all the values from the fields
             * and calls updateUser() passing the values as a User object
             */
            updateRecord.setOnClickListener {
                val firstName = firstName.text.trim().toString()
                val lastName = lastName.text.trim().toString()
                val email = email.text.trim().toString()
                val phoneNumber = phoneNumber.text.trim().toString()
                val password = password.text.trim().toString()
                val repeatPassword = repeatPassword.text.trim().toString()
                userDetailViewModel!!.updateUser(
                    firstName,
                    lastName,
                    email,
                    phoneNumber,
                    password,
                    repeatPassword
                )
            }

            microphoneImage.setOnClickListener {
                // Get the Intent action
                val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                // Language model defines the purpose, there are special models for other use cases, like search.
                sttIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                // Adding an extra language, you can use any language from the Locale class.
                sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                // Text that shows up on the Speech input prompt.
                sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Update the details")
                try {
                    // Start the intent for a result, and pass in our request code.
                    startActivityForResult(sttIntent, requestCodeStt)
                } catch (e: ActivityNotFoundException) {
                    // Handling error when the service is not available.
                    e.printStackTrace()

                    Toast.makeText(
                        requireContext(),
                        "Your device does not support STT.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        /**
         * Calls observe() every time LiveData closeFragment value is changed and passes result parameter
         */
        userDetailViewModel.closeFragment.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result)
                    this.findNavController().popBackStack()
        })

        /**
         * Calls observe() every time LiveData errorMessage value is changed and passes result parameter
         */
        userDetailViewModel.errorMessage.observe(viewLifecycleOwner, {result ->
            if (result != null) {
                // 0 - 15 are usually available on any device
                val musicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

                if (musicVolume == 0 || !response)
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                else
                    textToSpeech!!.speak(result, TextToSpeech.QUEUE_FLUSH,
                        null, null)
            }
        })

        userDetailViewModel.user.observe(viewLifecycleOwner, {pos ->
            when (pos.position) {
                'E' -> {
                    binding.spinner.setSelection(0)
                    userDetailViewModel.setPos(0)
                }
                'A' -> {
                    binding.spinner.setSelection(1)
                    userDetailViewModel.setPos(1)
                }
                else -> {
                    binding.spinner.setSelection(2)
                    userDetailViewModel.setPos(2)
                }
            }
        })

        userDetailViewModel.action.observe(viewLifecycleOwner, { num ->
            if (num != null) {
                if (num == 1) {
                    val firstName = binding.firstName.text.trim().toString()
                    val lastName = binding.lastName.text.trim().toString()
                    val email = binding.email.text.trim().toString()
                    val phoneNumber = binding.phoneNumber.text.trim().toString()
                    val password = binding.password.text.trim().toString()
                    val repeatPassword = binding.repeatPassword.text.trim().toString()
                    userDetailViewModel.updateUser(
                        firstName,
                        lastName,
                        email,
                        phoneNumber,
                        password,
                        repeatPassword
                    )
                }
            }
        })

        return binding.root
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
                        userDetailViewModel.convertStringToAction(recognizedText)
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set UK English as language for tts
            val result = textToSpeech!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.e("TTS", "The Language specified is not supported!")
            else
                response = true
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