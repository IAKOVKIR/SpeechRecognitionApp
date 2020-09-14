package com.example.audiochatbot.administrator.user_management

import android.os.Bundle
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

    private var textToSpeech: TextToSpeech? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: UserDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.user_detail, container, false)

        // Create an instance of the Application
        val application = requireNotNull(this.activity).application
        // Create an instance of the UserDetailFragmentArgs
        val arguments =
            UserDetailFragmentArgs.fromBundle(
                requireArguments()
            )

        textToSpeech = TextToSpeech(requireActivity(), this)

        // Create an instance of the ViewModel Factory.
        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        val viewModelFactory =
            UserDetailViewModelFactory(
                arguments.userKey,
                dataSource
            )

        // Get a reference to the ViewModel associated with this fragment.
        val userDetailViewModel =
            ViewModelProvider(
            this, viewModelFactory).get(UserDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.userDetailViewModel = userDetailViewModel
        binding.lifecycleOwner = this

        val adapter = ArrayAdapter(this.requireContext(),
            android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Positions))
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                userDetailViewModel.setPos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

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

        /**
         * Retrieves all the values from the fields
         * and calls updateUser() passing the values as a User object
         */
        binding.updateRecord.setOnClickListener {
            val firstName = binding.firstName.text.trim().toString()
            val lastName = binding.lastName.text.trim().toString()
            val email = binding.email.text.trim().toString()
            val phoneNumber = binding.phoneNumber.text.trim().toString()
            val password = binding.password.text.trim().toString()
            val repeatPassword = binding.repeatPassword.text.trim().toString()
            userDetailViewModel.updateUser(firstName, lastName, email, phoneNumber, password, repeatPassword)
        }

        /**
         * Calls deleteRecord() method
         */
        binding.deleteRecord.setOnClickListener {
            userDetailViewModel.deleteRecord()
        }

        /**
         * Calls observe() every time LiveData errorMessage value is changed and passes result parameter
         */
        userDetailViewModel.errorMessage.observe(viewLifecycleOwner, {result ->
            if (result != null) {
                textToSpeech!!.speak(
                    result,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        })

        /**
         * Calls observe() every time LiveData isUploaded value is changed and passes result parameter
         */
        userDetailViewModel.isUploaded.observe(viewLifecycleOwner, {result ->
            if (result) {
                //returns to the previous fragment
                this.findNavController().popBackStack()
            } else
                //send a toast message
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
        })

        return binding.root
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