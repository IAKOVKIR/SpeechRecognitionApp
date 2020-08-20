package com.example.audiochatbot.administrator.product_management

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.product_management.view_models.CreateProductViewModel
import com.example.audiochatbot.administrator.product_management.view_models.CreateProductViewModelFactory
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentCreateProductBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class CreateProductFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentCreateProductBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_product, container, false)
        val application = requireNotNull(this.activity).application
        val args = CreateProductFragmentArgs.fromBundle(requireArguments())
        val businessId: Int = args.businessId

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            CreateProductViewModelFactory(userDataSource)

        val productDetailViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(CreateProductViewModel::class.java)

        binding.submit.setOnClickListener {
            val product = Product()
            product.businessId = businessId
            product.name = binding.name.text.trim().toString()
            product.smallUnitName = binding.smallUnitName.text.toString().trim()
            product.bigUnitName = binding.bigUnitName.text.toString().trim()
            product.conversion = binding.conversion.text.toString().trim()
            product.price = binding.price.text.toString().toFloat()
            productDetailViewModel.submitProduct(product)
        }

        productDetailViewModel.isUploaded.observe(viewLifecycleOwner, { result ->
            if (result)
                this.findNavController().popBackStack()
            else {
                val toast = Toast.makeText(context, "Something went wrong :(", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()
            }
        })

        return binding.root
    }

}