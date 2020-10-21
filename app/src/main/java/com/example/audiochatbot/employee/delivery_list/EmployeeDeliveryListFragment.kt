package com.example.audiochatbot.employee.delivery_list

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import com.example.audiochatbot.R
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentEmployeeDeliveryListBinding
import com.example.audiochatbot.employee.delivery_list.recycler_view_adapters.EmployeeCancelDeliveryListener
import com.example.audiochatbot.employee.delivery_list.recycler_view_adapters.EmployeeDeliveryListRecyclerViewAdapter
import com.example.audiochatbot.employee.delivery_list.recycler_view_adapters.EmployeeDeliveryListener
import com.example.audiochatbot.employee.delivery_list.view_models.EmployeeDeliveryListViewModel
import com.example.audiochatbot.employee.delivery_list.view_models.EmployeeDeliveryListViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

class EmployeeDeliveryListFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var response = false
    private lateinit var testViewModel: EmployeeDeliveryListViewModel
    private val requestCodeStt = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentEmployeeDeliveryListBinding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_employee_delivery_list, container, false)

        val application = requireNotNull(this.activity).application
        val args = EmployeeDeliveryListFragmentArgs.fromBundle(requireArguments())
        val storeId: Int = args.storeId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        val viewModelFactory =
            EmployeeDeliveryListViewModelFactory(args.userId, storeId, dataSource)

        testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(EmployeeDeliveryListViewModel::class.java)

        val adapter =
            EmployeeDeliveryListRecyclerViewAdapter(
                EmployeeDeliveryListener { deliveryId ->
                    testViewModel.onDeliveryClicked(deliveryId)
                }, EmployeeCancelDeliveryListener { delivery ->
                    testViewModel.cancelDelivery(delivery)
                })
        binding.deliveryList.adapter = adapter

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
                startActivityForResult(sttIntent, requestCodeStt)
            } catch (e: ActivityNotFoundException) {
                // Handling error when the service is not available.
                e.printStackTrace()

                Toast.makeText(requireContext(), "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }

        binding.addNewDelivery.setOnClickListener {
            this.findNavController().navigate(
                EmployeeDeliveryListFragmentDirections.actionEmployeeDeliveryListToCreateDeliveryFragment(args.userId, storeId))
        }

        // Observers

        testViewModel.deliveries.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

        testViewModel.navigateToDeliveryDetails.observe(viewLifecycleOwner, { deliveryId ->
            deliveryId?.let {
                this.findNavController().navigate(EmployeeDeliveryListFragmentDirections.actionEmployeeDeliveryListToDeliveryDetailsFragment(deliveryId))
                testViewModel.onStoreNavigated()
            }
        })

        testViewModel.closeFragment.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result)
                    this.findNavController().popBackStack()
        })

        testViewModel.navigateToCreateNewDelivery.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result) {
                    this.findNavController().navigate(
                        EmployeeDeliveryListFragmentDirections.actionEmployeeDeliveryListToCreateDeliveryFragment(args.userId, storeId))
                    testViewModel.onStoreNavigated()
                }
        })

        testViewModel.message.observe(viewLifecycleOwner, { result ->
            if (result != null) {
                // 0 - 15 are usually available on any device
                val musicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

                if (musicVolume == 0 || !response)
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                else
                    textToSpeech!!.speak(result, TextToSpeech.QUEUE_FLUSH, null, null)
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
                        testViewModel.convertStringToAction(recognizedText)
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
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    override fun onResume() {
        super.onResume()
        testViewModel.refreshTheList()
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