package com.example.audiochatbot.administrator.discard_items

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
import com.example.audiochatbot.administrator.discard_items.recycler_view_adapters.DiscardedItemListRecyclerViewAdapter
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemListViewModel
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemListViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentDiscardItemListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

class DiscardItemListFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var response = false
    private lateinit var testViewModel: DiscardItemListViewModel
    private val requestCodeStt = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentDiscardItemListBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_discard_item_list, container, false)

        val application = requireNotNull(this.activity).application
        val args = DiscardItemListFragmentArgs.fromBundle(requireArguments())
        val storeId: Int = args.storeId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        val viewModelFactory =
            DiscardItemListViewModelFactory(storeId, dataSource)

        testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DiscardItemListViewModel::class.java)

        val adapter =
            DiscardedItemListRecyclerViewAdapter(dataSource)
        binding.discardedItemsList.adapter = adapter

        binding.microphoneImage.setOnClickListener {
            // Get the Intent action
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // Language model defines the purpose, there are special models for other use cases, like search.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Adding an extra language, you can use any language from the Locale class.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // Text that shows up on the Speech input prompt.
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "discard items")
            try {
                // Start the intent for a result, and pass in our request code.
                startActivityForResult(sttIntent, requestCodeStt)
            } catch (e: ActivityNotFoundException) {
                // Handling error when the service is not available.
                e.printStackTrace()

                Toast.makeText(requireContext(), "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }

        binding.discardItems.setOnClickListener {
            this.findNavController().navigate(DiscardItemListFragmentDirections.actionDiscardItemListToDiscardItem(args.adminId, storeId, args.businessId))
            testViewModel.onStoreNavigated()
        }

        // Observers

        testViewModel.discardedItems.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.closeFragment.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result)
                    this.findNavController().popBackStack()
        })

        testViewModel.navigateToDiscardItems.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result) {
                    this.findNavController().navigate(DiscardItemListFragmentDirections.actionDiscardItemListToDiscardItem(args.adminId, storeId, args.businessId))
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