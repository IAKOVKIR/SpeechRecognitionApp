package com.example.audiochatbot.administrator.product_management

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
import com.example.audiochatbot.administrator.product_management.view_models.ProductDetailViewModel
import com.example.audiochatbot.administrator.product_management.view_models.ProductDetailViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentProductDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ProductDetailFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var response = false
    private lateinit var productDetailViewModel: ProductDetailViewModel
    private val requestCodeStt = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentProductDetailBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_detail, container, false)
        val application = requireNotNull(this.activity).application
        val arguments = ProductDetailFragmentArgs.fromBundle(requireArguments())
        val storeId = arguments.storeKey

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        if (storeId == -1) {
            binding.sale.isEnabled = false
            binding.quantity.isEnabled = false
        }

        // Create an instance of the ViewModel Factory.
        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        val viewModelFactory =
            ProductDetailViewModelFactory(arguments.productKey, storeId, dataSource)

        // Get a reference to the ViewModel associated with this fragment.
        productDetailViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(ProductDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.productDetailViewModel = productDetailViewModel

        binding.lifecycleOwner = this

        binding.updateRecord.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val smallUnitName = binding.smallUnitName.text.toString().trim()
            val bigUnitName = binding.bigUnitName.text.toString().trim()
            val conversion = binding.conversion.text.toString().trim()
            val priceText = binding.price.text.toString()
            var price = 0F

            if (priceText != "") {
                price = binding.price.text.toString().toFloat()
            }

            if (storeId != -1) {
                val sale = binding.sale.text.toString().toInt()
                val quantity = binding.quantity.text.toString().toInt()
                productDetailViewModel.updateProduct(name, smallUnitName, bigUnitName, conversion, price, sale, quantity)
            } else
                productDetailViewModel.submitProduct(name, smallUnitName, bigUnitName, conversion, price)
        }

        binding.microphoneImage.setOnClickListener {
            // Get the Intent action
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // Language model defines the purpose, there are special models for other use cases, like search.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
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

                Toast.makeText(requireContext(), "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }

        binding.deleteRecord.setOnClickListener {
            productDetailViewModel.deleteRecord()
        }

        productDetailViewModel.assignedProduct.observe(viewLifecycleOwner, {assignedProduct ->
            if (assignedProduct == null) {
                binding.sale.isEnabled = false
                binding.quantity.isEnabled = false
            }
        })

        /**
         * Calls observe() every time LiveData errorMessage value is changed and passes result parameter
         */
        productDetailViewModel.errorMessage.observe(viewLifecycleOwner, {result ->
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

        productDetailViewModel.action.observe(viewLifecycleOwner, { num ->
            if (num != null) {
                if (num == 1) {
                    val name = binding.name.text.toString().trim()
                    val smallUnitName = binding.smallUnitName.text.toString().trim()
                    val bigUnitName = binding.bigUnitName.text.toString().trim()
                    val conversion = binding.conversion.text.toString().trim()
                    val priceText = binding.price.text.toString()
                    var price = 0F

                    if (priceText != "") {
                        price = binding.price.text.toString().toFloat()
                    }

                    if (storeId != -1) {
                        val sale = binding.sale.text.toString().toInt()
                        val quantity = binding.quantity.text.toString().toInt()
                        productDetailViewModel.updateProduct(name, smallUnitName, bigUnitName, conversion, price, sale, quantity)
                    } else
                        productDetailViewModel.submitProduct(name, smallUnitName, bigUnitName, conversion, price)
                }
            }
        })

        productDetailViewModel.closeFragment.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result)
                    this.findNavController().popBackStack()
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
                        productDetailViewModel.convertStringToAction(recognizedText)
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