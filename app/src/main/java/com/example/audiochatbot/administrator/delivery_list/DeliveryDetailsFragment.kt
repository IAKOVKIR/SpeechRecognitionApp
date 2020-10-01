package com.example.audiochatbot.administrator.delivery_list

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters.AcceptDeliveryProductsListener
import com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters.DeclineDeliveryProductsListener
import com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters.DeliveryDetailsRecyclerViewAdapter
import com.example.audiochatbot.administrator.delivery_list.view_models.DeliveryDetailsViewModel
import com.example.audiochatbot.administrator.delivery_list.view_models.DeliveryDetailsViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentDeliveryDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class DeliveryDetailsFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var response = false
    private lateinit var testViewModel: DeliveryDetailsViewModel
    private val requestCodeStt = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentDeliveryDetailsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_delivery_details, container, false)

        val application = requireNotNull(this.activity).application
        val args = DeliveryDetailsFragmentArgs.fromBundle(requireArguments())

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        val viewModelFactory =
            DeliveryDetailsViewModelFactory(args.deliveryId, dataSource)

        testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DeliveryDetailsViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.deliveryDetailViewModel = testViewModel

        binding.lifecycleOwner = this

        val adapter =
            DeliveryDetailsRecyclerViewAdapter(args.deliveryId
                , AcceptDeliveryProductsListener { product ->
                    testViewModel.acceptItems(product)
                }, DeclineDeliveryProductsListener { product ->
                    testViewModel.declineItems(product)
                }, dataSource)
        binding.deliveryList.adapter = adapter

        testViewModel.deliveryProducts.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.closeFragment.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result)
                    this.findNavController().popBackStack()
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