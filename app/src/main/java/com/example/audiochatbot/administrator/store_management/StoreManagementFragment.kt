package com.example.audiochatbot.administrator.store_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.store_management.view_models.StoreManagementViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreManagementViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentStoreManagementBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class StoreManagementFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentStoreManagementBinding = DataBindingUtil.inflate(inflater,
        R.layout.fragment_store_management, container, false)

        val application = requireNotNull(this.activity).application
        val args = StoreManagementFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.myId

        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            StoreManagementViewModelFactory(adminId, dataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(StoreManagementViewModel::class.java)

        testViewModel.navigateToStoreDetails.observe(viewLifecycleOwner, Observer { storeId ->
            storeId?.let {
                this.findNavController().navigate(StoreManagementFragmentDirections.actionStoreManagementToStoreDetail(storeId, adminId))
                testViewModel.onStoreNavigated()
            }
        })

        val adapter =
            StoreManagementFragmentRecyclerViewAdapter(
                StoreListener { storeId ->
                    testViewModel.onStoreClicked(storeId)
                })
        binding.storeList.adapter = adapter

        binding.createNewStore.setOnClickListener {
            this.findNavController().navigate(StoreManagementFragmentDirections.actionStoreManagementToCreateStoreFragment(adminId))
        }

        testViewModel.stores.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}