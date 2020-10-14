package com.example.audiochatbot.administrator.store_management

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
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
import com.example.audiochatbot.administrator.store_management.view_models.StoreDetailViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreDetailViewModelFactory
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentStoreDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class StoreDetailFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private lateinit var testViewModel: StoreDetailViewModel
    private var response = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentStoreDetailBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_store_detail, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource =
            UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        val args = StoreDetailFragmentArgs.fromBundle(requireArguments())
        val adminKey: Int = args.adminId
        val storeKey: Int = args.storeKey
        val businessId: Int = args.businessId

        val viewModelFactory =
            StoreDetailViewModelFactory(storeKey, dataSource)

        testViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(StoreDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.storeDetailViewModel = testViewModel

        binding.lifecycleOwner = this

        binding.updateRecord.setOnClickListener {
            val store = Store()
            store.street = binding.storeStreet.text.trim().toString()
            store.city = binding.storeCity.text.trim().toString()
            store.state = binding.storeState.text.trim().toString()
            store.phoneNumber = binding.storePhone.text.trim().toString()
            store.zip_code = binding.zipCode.text.toString().toInt()
            store.cashOnHand = binding.cashOnHand.text.toString().toFloat()
            testViewModel.updateStore(store)
        }

        binding.deleteRecord.setOnClickListener {
            testViewModel.deleteRecord()
        }

        binding.assignedUsers.setOnClickListener {
            this.findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailToAssignedUsersFragment(storeKey, adminKey, businessId))
        }

        binding.assignedProducts.setOnClickListener {
            this.findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailToAssignedProductsFragment(storeKey, adminKey, businessId))
        }

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