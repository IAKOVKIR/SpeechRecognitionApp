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
import com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters.*
import com.example.audiochatbot.administrator.delivery_list.view_models.CreateDeliveryViewModel
import com.example.audiochatbot.administrator.delivery_list.view_models.CreateDeliveryViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentCreateDeliveryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class CreateDeliveryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentCreateDeliveryBinding = DataBindingUtil.inflate(inflater,
        R.layout.fragment_create_delivery, container, false)

        val application = requireNotNull(this.activity).application
        val args = CreateDeliveryFragmentArgs.fromBundle(requireArguments())

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            CreateDeliveryViewModelFactory(args.adminId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(CreateDeliveryViewModel::class.java)

        var adapter =
            CreateDeliveryRecyclerViewAdapter(
                AddDeliveryProductListener { product, smallQuantity, bigQuantity ->
                    testViewModel.addItem(product.productId, smallQuantity, bigQuantity)
                },
                RemoveDeliveryProductListener {
                    testViewModel.removeItem(it.productId)
                }, List(100) { 0 }
            )
        binding.deliveryList.adapter = adapter

        testViewModel.products.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.l.observe(viewLifecycleOwner, { l ->
            adapter =
                CreateDeliveryRecyclerViewAdapter(
                    AddDeliveryProductListener { product, smallQuantity, bigQuantity ->
                        testViewModel.addItem(product.productId, smallQuantity, bigQuantity)
                    },
                    RemoveDeliveryProductListener {
                        testViewModel.removeItem(it.productId)
                    }, l
                )
            binding.deliveryList.adapter = adapter
        })

        binding.submitTheDelivery.setOnClickListener {
            testViewModel.submitDelivery()
        }

        testViewModel.isDone.observe(viewLifecycleOwner, { result ->
            if (result) {
                this.findNavController().popBackStack()
                this.findNavController().popBackStack()
            }
        })

        return binding.root
    }
}