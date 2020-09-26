package com.example.audiochatbot.administrator.cash_report

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.cash_report.view_models.CashReportViewModel
import com.example.audiochatbot.administrator.cash_report.view_models.CashReportViewModelFactory
import com.example.audiochatbot.administrator.cash_report.recycler_view_adapters.CashReportRecyclerViewAdapter
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentCashReportBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class CashReportFragment : Fragment() {

    private val requestCodeStt = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentCashReportBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_cash_report, container, false)

        val application = requireNotNull(this.activity).application
        val args = CashReportFragmentArgs.fromBundle(requireArguments())
        val storeId: Int = args.storeId
        val adminId: Int = args.adminId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            CashReportViewModelFactory(adminId, storeId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(CashReportViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.cashReportViewModel = testViewModel

        binding.lifecycleOwner = this

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

        val adapter =
            CashReportRecyclerViewAdapter(dataSource)
        binding.cashReportList.adapter = adapter

        testViewModel.cashReports.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.deposit.setOnClickListener {
            val amount = binding.amountField.text.toString()
            if (amount!= "") {
                testViewModel.depositOrWithdrawMoney(amount.toFloat(), true)
                binding.amountField.text = null
            }
        }

        binding.withdraw.setOnClickListener {
            val amount = binding.amountField.text.toString()
            if (amount!= "") {
                testViewModel.depositOrWithdrawMoney(amount.toFloat(), false)
                binding.amountField.text = null
            }
        }

        return binding.root
    }
}