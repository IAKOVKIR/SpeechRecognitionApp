package com.example.audiochatbot.administrator.delivery_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters.CancelDeliveryListener
import com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters.DeliveryListRecyclerViewAdapter
import com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters.DeliveryListener
import com.example.audiochatbot.administrator.delivery_list.view_models.DeliveryListViewModel
import com.example.audiochatbot.administrator.delivery_list.view_models.DeliveryListViewModelFactory
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
        val adminId: Int = args.adminId
        val businessId: Int = args.businessId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            DeliveryListViewModelFactory(businessId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DeliveryListViewModel::class.java)

        val adapter =
            DeliveryListRecyclerViewAdapter(
                DeliveryListener { deliveryId ->
                    testViewModel.onDeliveryClicked(deliveryId)
                }, CancelDeliveryListener { delivery ->
                    testViewModel.cancelDelivery(delivery)
                })
        binding.deliveryList.adapter = adapter

        testViewModel.deliveries.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.navigateToDeliveryDetails.observe(viewLifecycleOwner, { deliveryId ->
            deliveryId?.let {
                this.findNavController().navigate(DeliveryListFragmentDirections.actionDeliveryListToDeliveryDetailsFragment(adminId, deliveryId, businessId))
                testViewModel.onStoreNavigated()
            }
        })

        binding.addNewDelivery.setOnClickListener {
            this.findNavController().navigate(DeliveryListFragmentDirections.actionDeliveryListToDiscardItemStore(adminId, businessId, 1))
        }

        return binding.root
    }

}