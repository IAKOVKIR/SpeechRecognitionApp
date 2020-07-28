package com.example.audiochatbot.administrator.product_management

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
import com.example.audiochatbot.administrator.product_management.view_models.ProductManagementViewModel
import com.example.audiochatbot.administrator.product_management.view_models.ProductManagementViewModelFactory
import com.example.audiochatbot.administrator.user_management.UserManagementFragmentArgs
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentProductManagementBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class ProductManagementFragment : Fragment() {

    private lateinit var testViewModel: ProductManagementViewModel
    private var line = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentProductManagementBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_product_management, container, false)

        val application = requireNotNull(this.activity).application
        val args = UserManagementFragmentArgs.fromBundle(requireArguments())
        //val adminId: Int = args.adminId
        val businessId: Int = args.businessId

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            ProductManagementViewModelFactory(businessId, userDataSource)

        testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(ProductManagementViewModel::class.java)

        testViewModel.navigateToProductDetails.observe(viewLifecycleOwner, Observer { productId ->
            productId?.let {
                this.findNavController().navigate(ProductManagementFragmentDirections.actionProductManagementToProductDetailFragment(productId))
                testViewModel.onProductNavigated()
            }
        })

        val adapter =
            ProductManagementRecyclerViewAdapter(
                ProductListener { productId ->
                    testViewModel.onProductClicked(productId)
                })
        binding.userList.adapter = adapter

        binding.findProduct.setOnClickListener {
            line = binding.storeId.text.toString().trim()
            testViewModel.retrieveList(line)
        }

        testViewModel.products.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        testViewModel.retrieveList(line)
    }
}