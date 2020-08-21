package com.example.audiochatbot.administrator.store_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.*
import com.example.audiochatbot.administrator.store_management.view_models.AssignedProductsViewModel
import com.example.audiochatbot.administrator.store_management.view_models.AssignedProductsViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentAssignedProductsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class AssignedProductsFragment : Fragment() {

    private var storeId: Int? = null
    private var adminId: Int? = null
    private var businessId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = AssignedProductsFragmentArgs.fromBundle(requireArguments())
        storeId = args.storeId
        adminId = args.adminId
        businessId = args.businessId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAssignedProductsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_assigned_products, container, false)

        val application = requireNotNull(this.activity).application

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            AssignedProductsViewModelFactory(storeId!!, userDataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(AssignedProductsViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = testViewModel

        binding.lifecycleOwner = this

        val adapter =
            AssignedProductsRecyclerViewAdapter(
                AssignedProductListener { productId ->
                    testViewModel.onProductClicked(productId)
                },
                RemoveProductListener { productId ->
                    testViewModel.deleteRecord(productId)
                }, storeId!!, userDataSource)
        binding.userList.adapter = adapter

        testViewModel.products.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.navigateToProductDetails.observe(viewLifecycleOwner, { productId ->
            productId?.let {
                this.findNavController().navigate(AssignedProductsFragmentDirections.actionAssignedProductsToProductDetail(productId, storeId!!))
                testViewModel.onProductNavigated()
            }
        })

        binding.assignProducts.setOnClickListener {
            this.findNavController().navigate(AssignedProductsFragmentDirections.actionAssignedProductsToAssignProducts(adminId!!, storeId!!, businessId!!))
        }

        return binding.root
    }

}