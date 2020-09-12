package com.example.audiochatbot.administrator.store_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.store_management.view_models.StoreDetailViewModel
import com.example.audiochatbot.administrator.store_management.view_models.StoreDetailViewModelFactory
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentStoreDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class StoreDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentStoreDetailBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_store_detail, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource =
            UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val args = StoreDetailFragmentArgs.fromBundle(requireArguments())
        val adminKey: Int = args.adminId
        val storeKey: Int = args.storeKey

        val viewModelFactory =
            StoreDetailViewModelFactory(storeKey, dataSource)

        val viewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(StoreDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.storeDetailViewModel = viewModel

        binding.lifecycleOwner = this

        binding.updateRecord.setOnClickListener {
            val store = Store()
            store.street = binding.storeStreet.text.trim().toString()
            store.city = binding.storeCity.text.trim().toString()
            store.state = binding.storeState.text.trim().toString()
            store.phoneNumber = binding.storePhone.text.trim().toString()
            store.zip_code = binding.zipCode.text.toString().toInt()
            store.cashOnHand = binding.cashOnHand.text.toString().toFloat()
            viewModel.updateStore(store)
        }

        binding.deleteRecord.setOnClickListener {
            viewModel.deleteRecord()
        }

        binding.assignedUsers.setOnClickListener {
            this.findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailToAssignedUsersFragment(storeKey, adminKey, args.businessId))
        }

        binding.assignedProducts.setOnClickListener {
            this.findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailToAssignedProductsFragment(storeKey, adminKey, args.businessId))
        }

        viewModel.isDone.observe(viewLifecycleOwner, { result ->
            if (result)
                this.findNavController().popBackStack()
            else
                Toast.makeText(context, "Something went wrong :(", Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }

}