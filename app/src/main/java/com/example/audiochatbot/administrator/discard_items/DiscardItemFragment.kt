package com.example.audiochatbot.administrator.discard_items

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemViewModel
import com.example.audiochatbot.administrator.discard_items.view_models.DiscardItemViewModelFactory
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
    private var adminId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = DiscardItemFragmentArgs.fromBundle(requireArguments())
        storeId = args.storeId
        businessId = args.businessId
        adminId = args.adminId
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
            DiscardItemViewModelFactory(storeId!!, userDataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(DiscardItemViewModel::class.java)

        val adapter =
            DiscardItemRecyclerViewAdapter(
                DiscardProductListener { productId, quantity ->
                    testViewModel.discardItem(productId, adminId!!, quantity)
                })
        binding.productList.adapter = adapter

        testViewModel.products.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.message.observe(viewLifecycleOwner, { result ->
            if (result != null)
                displayMessage(result)
        })

        return binding.root
    }

    private fun displayMessage(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.show()
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DiscardItemViewModel::class.java)
    }*/

}