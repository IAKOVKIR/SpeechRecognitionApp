package com.example.audiochatbot.administrator.delivery_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.store_management.StoreManagementFragmentArgs
import com.example.audiochatbot.administrator.store_management.view_models.StoreManagementViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreManagementViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.DeliveryListFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class DeliveryListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: DeliveryListFragmentBinding = DataBindingUtil.inflate(inflater,
            R.layout.delivery_list_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val args = DeliveryListFragmentArgs.fromBundle(requireArguments())
        val businessId: Int = args.businessId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            DeliveryListViewModelFactory(businessId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DeliveryListViewModel::class.java)

        val adapter =
            DeliveryListRecyclerViewAdapter(
                /*StoreListener { storeId ->
                    testViewModel.onStoreClicked(storeId)
                }*/)
        binding.deliveryList.adapter = adapter

        testViewModel.deliveries.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }

}