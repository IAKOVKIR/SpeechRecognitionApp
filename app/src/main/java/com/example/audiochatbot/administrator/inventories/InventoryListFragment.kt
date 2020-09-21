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
import com.example.audiochatbot.administrator.inventories.recycler_view_adapters.InventoryListRecyclerViewAdapter
import com.example.audiochatbot.administrator.inventories.view_models.InventoryListViewModel
import com.example.audiochatbot.administrator.inventories.view_models.InventoryListViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentInventoryListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class InventoryListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentInventoryListBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_inventory_list, container, false)

        val application = requireNotNull(this.activity).application
        val args = InventoryListFragmentArgs.fromBundle(requireArguments())

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            InventoryListViewModelFactory(args.businessId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(InventoryListViewModel::class.java)

        val adapter = InventoryListRecyclerViewAdapter()
        binding.deliveryList.adapter = adapter

        testViewModel.inventories.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.addNewDelivery.setOnClickListener {
            this.findNavController().navigate(InventoryListFragmentDirections.actionInventoryListToInventoryCount(args.adminId, args.storeId))
        }

        return binding.root
    }
}