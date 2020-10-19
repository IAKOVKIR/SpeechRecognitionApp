package com.example.audiochatbot.administrator.store_management

import android.app.Activity
import android.content.ActivityNotFoundException
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
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AddProductListener
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AssignProductListener
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AssignProductsRecyclerViewAdapter
import com.example.audiochatbot.administrator.store_management.view_models.AssignProductsViewModel
import com.example.audiochatbot.administrator.store_management.view_models.AssignProductsViewModelFactory
import com.example.audiochatbot.administrator.store_management.view_models.AssignUsersViewModel
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentAssignProductsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class AssignProductsFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private lateinit var testViewModel: AssignUsersViewModel
    private val requestCodeStt = 1
    private var response = false

    private var storeId: Int? = null
    private var businessId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = AssignedProductsFragmentArgs.fromBundle(requireArguments())
        storeId = args.storeId
        businessId = args.businessId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAssignProductsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_assign_products, container, false)

        val application = requireNotNull(this.activity).application
        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        val viewModelFactory =
            AssignProductsViewModelFactory(storeId!!, businessId!!, userDataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(AssignProductsViewModel::class.java)

        val adapter =
            AssignProductsRecyclerViewAdapter(
                AssignProductListener { productId ->
                    testViewModel.onProductClicked(productId)
                },
                AddProductListener { productId ->
                    testViewModel.addRecord(productId)
                })
        binding.productList.adapter = adapter

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

        testViewModel.products.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.navigateToProductDetails.observe(viewLifecycleOwner, { productId ->
            productId?.let {
                this.findNavController().navigate(AssignProductsFragmentDirections.actionAssignProductsToProductDetail(productId, storeId!!))
                testViewModel.onProductNavigated()
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