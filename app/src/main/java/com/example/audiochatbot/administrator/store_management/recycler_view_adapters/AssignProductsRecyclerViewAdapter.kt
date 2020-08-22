package com.example.audiochatbot.administrator.store_management.recycler_view_adapters

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.AssignedProduct
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentAssignProductsRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 */
class AssignProductsRecyclerViewAdapter(private val clickListener: AssignProductListener,
                                        private val userDao: UserDao, private val storeId: Int
) : ListAdapter<Product,
        AssignProductsRecyclerViewAdapter.ViewHolder>(AssignProductsDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item, userDao, storeId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentAssignProductsRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AssignProductListener, item: Product, userDao: UserDao, storeId: Int) {
            binding.product = item
            binding.clickListener = clickListener
            binding.namePrice.text = "${item.name}   A$${item.price}"
            binding.quantity.text = "Quantity: "
            binding.addButton.isEnabled = false

            var bool: Boolean

            CoroutineScope(Dispatchers.Default).launch {

                var num: Int

                withContext(Dispatchers.IO) {
                    bool = userDao.ifProductAssigned(item.productId, storeId) == 0
                    num = userDao.totalProductQuantity(item.productId)
                }
                launch(Dispatchers.Main) {
                    binding.addButton.isEnabled = bool
                    binding.quantity.text = "Total Quantity: $num"
                }
            }

            binding.addButton.setOnClickListener{
                CoroutineScope(Dispatchers.Default).launch {
                    withContext(Dispatchers.IO) {
                        val num = userDao.getLastAssignedProductId() + 1
                        userDao.assignProduct(AssignedProduct(num, item.productId, storeId, 0, 0,"30/07/2020", "12:40"))
                        bool = userDao.ifProductAssigned(item.productId, storeId) == 0
                    }
                    launch(Dispatchers.Main) {
                        binding.addButton.isEnabled = bool
                    }
                }
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentAssignProductsRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class AssignProductsDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class AssignProductListener(val clickListener: (productId: Int) -> Unit) {
    fun onClick(product: Product) = clickListener(product.productId)
}