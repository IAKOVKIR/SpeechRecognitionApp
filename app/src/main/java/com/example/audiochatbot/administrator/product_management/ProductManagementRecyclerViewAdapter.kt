package com.example.audiochatbot.administrator.product_management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.databinding.FragmentProductManagementRecyclerViewAdapterBinding

class ProductManagementRecyclerViewAdapter(private val clickListener: ProductListener) : ListAdapter<Product,
        ProductManagementRecyclerViewAdapter.ViewHolder>(
    ProductDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: FragmentProductManagementRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: ProductListener, item: Product) {
            binding.product = item
            binding.clickListener = clickListener
            binding.namePrice.text = "${item.name}   A$${item.price}"

            /*CoroutineScope(Dispatchers.Default).launch {

                withContext(Dispatchers.Default) {
                    followerDataSource.deleteRecord(userId, selectedUserId)
                }

                launch (Dispatchers.Main) {
                    if (res) {
                        binding.followUnFollowButton.text = str[0]
                        bool = -1
                    }
                }
            }*/
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentProductManagementRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
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