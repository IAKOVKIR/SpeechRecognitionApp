package com.example.audiochatbot.administrator.inventories

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Environment
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
import com.example.audiochatbot.administrator.inventories.recycler_view_adapters.DownloadTheReportListener
import com.example.audiochatbot.administrator.inventories.recycler_view_adapters.InventoryListRecyclerViewAdapter
import com.example.audiochatbot.administrator.inventories.view_models.InventoryListViewModel
import com.example.audiochatbot.administrator.inventories.view_models.InventoryListViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentInventoryListBinding
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * A [Fragment] subclass that displays list of inventory counts and the button to start a new inventory count.
 */
class InventoryListFragment : Fragment(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var response = false
    private lateinit var testViewModel: InventoryListViewModel
    private val requestCodeStt = 1
    private val storageCode: Int = 100
    var list: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentInventoryListBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_inventory_list, container, false)

        val application = requireNotNull(this.activity).application
        val args = InventoryListFragmentArgs.fromBundle(requireArguments())

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        // Get the AudioManager service
        val audio = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        textToSpeech = TextToSpeech(requireActivity(), this)

        val viewModelFactory =
            InventoryListViewModelFactory(args.storeId, dataSource)

        testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(InventoryListViewModel::class.java)

        val adapter = InventoryListRecyclerViewAdapter(DownloadTheReportListener { inventoryCount ->
            testViewModel.generateAReport(inventoryCount)
        })
        binding.deliveryList.adapter = adapter

        testViewModel.inventories.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.closeFragment.observe(viewLifecycleOwner, { result ->
            if (result != null)
                if (result)
                    this.findNavController().popBackStack()
        })

        testViewModel.navigateToInventoryCount.observe(viewLifecycleOwner, { deliveryId ->
            deliveryId?.let {
                this.findNavController().navigate(InventoryListFragmentDirections.actionInventoryListToInventoryCount(args.adminId, args.storeId))
                testViewModel.onInventoryCountNavigated()
            }
        })

        testViewModel.reportList.observe(viewLifecycleOwner, { result ->
            if (result != null) {
                list = result
                savePdf()
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

        // buttons

        binding.inventoryCount.setOnClickListener {
            this.findNavController().navigate(InventoryListFragmentDirections.actionInventoryListToInventoryCount(args.adminId, args.storeId))
            testViewModel.onInventoryCountNavigated()
        }

        binding.microphoneImage.setOnClickListener {
            // Get the Intent action
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // Language model defines the purpose, there are special models for other use cases, like search.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Adding an extra language, you can use any language from the Locale class.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // Text that shows up on the Speech input prompt.
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "inventory count")
            try {
                // Start the intent for a result, and pass in our request code.
                startActivityForResult(sttIntent, requestCodeStt)
            } catch (e: ActivityNotFoundException) {
                // Handling error when the service is not available.
                e.printStackTrace()

                Toast.makeText(requireContext(), "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    private fun savePdf() {
        //create object of Document class
        val mDoc = Document()
        //pdf file name
        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        //pdf file path
        val mFilePath = requireContext().getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + "/inventory_count_" + mFileName +".pdf"
        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))

            //open the document for writing
            mDoc.open()

            //add content from the list
            for (i in list!!)
                mDoc.add(Paragraph(i))

            //close document
            mDoc.close()

            //show file saved message with file name and path
            Toast.makeText(requireActivity(), "$mFileName.pdf\nis saved to\n$mFilePath", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            //if anything goes wrong causing exception, get and show exception message
            Toast.makeText(requireActivity(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            storageCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted, call savePdf() method
                    savePdf()
                }
                else {
                    //permission from popup was denied, show error message
                    Toast.makeText(requireActivity(), "Permission denied...!", Toast.LENGTH_SHORT).show()
                }
            }
        }
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