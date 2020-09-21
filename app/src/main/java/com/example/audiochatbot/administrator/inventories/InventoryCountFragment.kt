package com.example.audiochatbot.administrator.inventories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.inventories.recycler_view_adapters.AddInventoryListener
import com.example.audiochatbot.administrator.inventories.recycler_view_adapters.InventoryCountRecyclerViewAdapter
import com.example.audiochatbot.administrator.inventories.recycler_view_adapters.RemoveInventoryListener
import com.example.audiochatbot.administrator.inventories.view_models.InventoryCountViewModel
import com.example.audiochatbot.administrator.inventories.view_models.InventoryCountViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentInventoryCountBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class InventoryCountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentInventoryCountBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_inventory_count, container, false)

        val application = requireNotNull(this.activity).application
        val args = InventoryCountFragmentArgs.fromBundle(requireArguments())

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            InventoryCountViewModelFactory(args.adminId, args.storeId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(InventoryCountViewModel::class.java)

        var adapter =
            InventoryCountRecyclerViewAdapter(
                AddInventoryListener { product, smallQuantity, bigQuantity ->
                    testViewModel.addItem(product.productId, smallQuantity, bigQuantity)
                },
                RemoveInventoryListener {
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
                InventoryCountRecyclerViewAdapter(
                    AddInventoryListener { product, smallQuantity, bigQuantity ->
                        testViewModel.addItem(product.productId, smallQuantity, bigQuantity)
                    },
                    RemoveInventoryListener {
                        testViewModel.removeItem(it.productId)
                    }, l
                )
            binding.deliveryList.adapter = adapter
        })

        binding.submitTheInventoryCount.setOnClickListener {
            val amount = binding.amount.text.toString().toFloat()
            testViewModel.submitInventoryCount(amount)
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