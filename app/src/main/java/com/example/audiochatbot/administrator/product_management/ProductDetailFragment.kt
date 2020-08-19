package com.example.audiochatbot.administrator.product_management

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
import com.example.audiochatbot.administrator.product_management.view_models.ProductDetailViewModel
import com.example.audiochatbot.administrator.product_management.view_models.ProductDetailViewModelFactory
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentProductDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class ProductDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentProductDetailBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_detail, container, false)
        val application = requireNotNull(this.activity).application
        val arguments = ProductDetailFragmentArgs.fromBundle(requireArguments())

        // Create an instance of the ViewModel Factory.
        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        val viewModelFactory =
            ProductDetailViewModelFactory(arguments.productKey, dataSource)

        // Get a reference to the ViewModel associated with this fragment.
        val productDetailViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(ProductDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.productDetailViewModel = productDetailViewModel

        binding.lifecycleOwner = this

        binding.updateRecord.setOnClickListener {
            val product = Product()
            product.name = binding.name.text.trim().toString()
            product.smallUnitName = binding.smallUnitName.text.toString().trim()
            product.bigUnitName = binding.bigUnitName.text.toString().trim()
            val quantity = binding.quantity.text.toString().toInt()
            product.conversion = binding.conversion.text.toString().trim()
            product.price = binding.price.text.toString().toFloat()
            val sale = binding.sale.text.toString().toInt()
            productDetailViewModel.updateProduct(product)
        }

        binding.deleteRecord.setOnClickListener {
            productDetailViewModel.deleteRecord()
        }

        productDetailViewModel.isUploaded.observe(viewLifecycleOwner, Observer {result ->
            if (result)
                this.findNavController().popBackStack()
            else
                Toast.makeText(context, "Something went wrong :(", Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }
}