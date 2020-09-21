package com.example.audiochatbot.administrator.discard_items

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.discard_items.recycler_view_adapters.DiscardItemStoreRecyclerViewAdapter
import com.example.audiochatbot.administrator.discard_items.recycler_view_adapters.ItemStoreListener
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemStoreViewModel
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemStoreViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentDiscardItemStoreBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class DiscardItemStoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentDiscardItemStoreBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_discard_item_store, container, false)

        val application = requireNotNull(this.activity).application
        val args = DiscardItemStoreFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.adminId
        val businessId: Int = args.businessId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            DiscardItemStoreViewModelFactory(adminId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DiscardItemStoreViewModel::class.java)

        testViewModel.navigateToDiscardItem.observe(viewLifecycleOwner, { storeId ->
            storeId?.let {
                when (args.directionId) {
                    0 -> {
                        this.findNavController().navigate(
                            DiscardItemStoreFragmentDirections
                                .actionDiscardItemStoreToDiscardItemFragment(
                                    adminId, storeId, businessId)
                        )
                    }
                    1 -> {
                        this.findNavController().navigate(
                            DiscardItemStoreFragmentDirections
                                .actionDiscardItemStoreToDeliveryList(
                                    adminId, storeId, businessId)
                        )
                    }
                    else -> {
                        this.findNavController().navigate(
                            DiscardItemStoreFragmentDirections
                                .actionDiscardItemStoreToInventoryList(
                                    adminId, storeId, businessId)
                        )
                    }
                }
                testViewModel.onStoreNavigated()
            }
        })

        val adapter =
            DiscardItemStoreRecyclerViewAdapter(
                ItemStoreListener { storeId ->
                    testViewModel.onStoreClicked(storeId)
                })
        binding.storeList.adapter = adapter

        testViewModel.stores.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}