package com.example.audiochatbot.administrator.delivery_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.delivery_list.view_models.DeliveryDetailsViewModel
import com.example.audiochatbot.administrator.delivery_list.view_models.DeliveryDetailsViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentDeliveryDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class DeliveryDetailsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentDeliveryDetailsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_delivery_details, container, false)

        val application = requireNotNull(this.activity).application
        val args = DeliveryListFragmentArgs.fromBundle(requireArguments())
        val businessId: Int = args.businessId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            DeliveryDetailsViewModelFactory(businessId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DeliveryDetailsViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.deliveryDetailViewModel = testViewModel

        binding.lifecycleOwner = this

        val adapter =
            DeliveryDetailsRecyclerViewAdapter(
                AcceptDeliveryProductsListener { deliveryId ->
                    //testViewModel.onDeliveryClicked(deliveryId)
                }, DeclineDeliveryProductsListener {deliveryId ->

                })
        binding.deliveryList.adapter = adapter

        testViewModel.deliveryProducts.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })
        return binding.root
    }

}