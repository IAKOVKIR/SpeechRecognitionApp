package com.example.audiochatbot.administrator.product_management

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentProductManagementRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductManagementRecyclerViewAdapter(private val clickListener: ProductListener,
                                           private val database: UserDao) : ListAdapter<Product,
        ProductManagementRecyclerViewAdapter.ViewHolder>(
    ProductDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item, database)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentProductManagementRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: ProductListener, item: Product, database: UserDao) {
            binding.product = item
            binding.clickListener = clickListener
            binding.namePrice.text = context.getString(R.string.name_price, item.name, item.price)

            CoroutineScope(Dispatchers.Default).launch {

                var num: Int

                withContext(Dispatchers.Default) {
                    num = database.totalProductQuantity(item.productId)
                }

                launch (Dispatchers.Main) {
                    binding.quantity.text = context.getString(R.string.total_product_quantity, num)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentProductManagementRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class ProductListener(val clickListener: (productId: Int) -> Unit) {
    fun onClick(product: Product) = clickListener(product.productId)
}