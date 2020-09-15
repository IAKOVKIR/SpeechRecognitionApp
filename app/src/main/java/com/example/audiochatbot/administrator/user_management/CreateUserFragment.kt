package com.example.audiochatbot.administrator.user_management

import android.content.Context
import android.media.AudioManager
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
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModel
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentCreateUserBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class CreateUserFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentCreateUserBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_user, container, false)
        val application = requireNotNull(this.activity).application
        val args = CreateUserFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.adminId

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        textToSpeech = TextToSpeech(requireActivity(), this)

        val viewModelFactory =
            CreateUserViewModelFactory(userDataSource)

        val viewModel =
            ViewModelProvider(
                this, viewModelFactory).get(CreateUserViewModel::class.java)

        val adapter = ArrayAdapter(this.requireContext(),
            android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Positions))
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                viewModel.setPos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        binding.submit.setOnClickListener {
            val firstName = binding.firstName.text.trim().toString()
            val lastName = binding.lastName.text.trim().toString()
            val email = binding.email.text.trim().toString()
            val phoneNumber = binding.phoneNumber.text.trim().toString()
            val password = binding.password.text.trim().toString()
            viewModel.submitUser(firstName, lastName, email, phoneNumber, password, adminId)
        }

        /**
         * Calls observe() every time LiveData errorMessage value is changed and passes result parameter
         */
        viewModel.errorMessage.observe(viewLifecycleOwner, {result ->
            if (result != null) {
                // 0 - 15 are usually available on any device
                val musicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

                if (musicVolume == 0)
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                else
                    textToSpeech!!.speak(result, TextToSpeech.QUEUE_FLUSH,
                        null, null)
            }
        })

        viewModel.isUploaded.observe(viewLifecycleOwner, {result ->
            if (result)
                this.findNavController().popBackStack()
            else
                Toast.makeText(context, "Something went wrong :(", Toast.LENGTH_SHORT).show()
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