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
import com.example.audiochatbot.administrator.user_management.UserManagementFragmentDirections
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
        //val args = TestF.fromBundle(requireArguments())
        //val myID: Int = args.myId //myID

        val storeDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).storeDao

        val viewModelFactory =
            StoreManagementViewModelFactory(
                storeDataSource,
                application
            )

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(StoreManagementViewModel::class.java)

        testViewModel.navigateToStoreDetails.observe(viewLifecycleOwner, Observer { storeId ->
            storeId?.let {
                this.findNavController().navigate(
                    UserManagementFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(
                        storeId
                    )
                )
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
            this.findNavController().navigate(StoreManagementFragmentDirections.actionStoreManagementToCreateStoreFragment())
        }

        testViewModel.stores.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}