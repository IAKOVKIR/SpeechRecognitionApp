package com.example.audiochatbot.administrator.cash_report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

/**
 * A simple [Fragment] subclass.
 */
class CashReportFragment : Fragment() {

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