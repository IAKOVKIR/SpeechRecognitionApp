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
import com.example.audiochatbot.administrator.store_management.view_models.CreateStoreViewModel
import com.example.audiochatbot.administrator.store_management.view_models.CreateStoreViewModelFactory
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentCreateStoreBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class CreateStoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentCreateStoreBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_store, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val args = CreateStoreFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.adminId //adminId

        val viewModelFactory =
            CreateStoreViewModelFactory(adminId, dataSource)

        val viewModel =
            ViewModelProvider(
                this, viewModelFactory).get(CreateStoreViewModel::class.java)

        binding.submit.setOnClickListener {
            val store = Store()
            store.street = binding.storeStreet.text.trim().toString()
            store.city = binding.storeCity.text.trim().toString()
            store.state = binding.storeState.text.trim().toString()
            store.phoneNumber = binding.storePhone.text.trim().toString()
            store.zip_code = binding.zipCode.text.toString().toInt()
            store.cashOnHand = binding.cashOnHand.text.toString().toFloat()
            viewModel.addStore(store)
        }

        viewModel.isUploaded.observe(viewLifecycleOwner, Observer {result ->
            if (result)
                this.findNavController().popBackStack()
            else
                Toast.makeText(context, "Something went wrong :(", Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }
}