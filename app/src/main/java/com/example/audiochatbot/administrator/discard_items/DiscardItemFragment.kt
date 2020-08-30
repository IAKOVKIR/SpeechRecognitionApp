package com.example.audiochatbot.administrator.discard_items


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemViewModel
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemViewModelFactory
import com.example.audiochatbot.administrator.store_management.AssignedProductsFragmentArgs
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AddProductListener
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AssignProductListener
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AssignProductsRecyclerViewAdapter
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.DiscardItemFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class DiscardItemFragment : Fragment() {

    /*companion object {
        fun newInstance() = DiscardItemFragment()
    }

    private lateinit var viewModel: DiscardItemViewModel
     */

    private var storeId: Int? = null
    private var businessId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = AssignedProductsFragmentArgs.fromBundle(requireArguments())
        storeId = args.storeId
        businessId = args.businessId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: DiscardItemFragmentBinding = DataBindingUtil.inflate(inflater,
            R.layout.discard_item_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            DiscardItemViewModelFactory(storeId!!, businessId!!, userDataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DiscardItemViewModel::class.java)

        val adapter =
            AssignProductsRecyclerViewAdapter(
                AssignProductListener { productId ->
                    testViewModel.onProductClicked(productId)
                },
                AddProductListener { productId, quantity ->
                    //testViewModel.addRecord(productId, quantity)
                })
        binding.productList.adapter = adapter

        testViewModel.products.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.navigateToProductDetails.observe(viewLifecycleOwner, { productId ->
            productId?.let {
                //this.findNavController().navigate(AssignProductsFragmentDirections.actionAssignProductsToProductDetail(productId, storeId!!))
                testViewModel.onProductNavigated()
            }
        })

        return binding.root
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DiscardItemViewModel::class.java)
    }*/

}