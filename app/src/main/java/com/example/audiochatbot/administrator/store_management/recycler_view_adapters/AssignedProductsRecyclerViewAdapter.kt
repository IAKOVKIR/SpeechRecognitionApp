package com.example.audiochatbot.administrator.store_management.recycler_view_adapters

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.databinding.FragmentAssignedProductsRecyclerViewAdapterBinding

/**
 * A simple [Fragment] subclass.
 */
class AssignedProductsRecyclerViewAdapter(private val clickListener: AssignedProductListener,
                                          private val removeProductListener: RemoveProductListener
) : ListAdapter<Product,
        AssignedProductsRecyclerViewAdapter.ViewHolder>(
    AssignedProductsDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, removeProductListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentAssignedProductsRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AssignedProductListener, removeProductListener: RemoveProductListener, item: Product) {
            binding.product = item
            binding.clickListener = clickListener
            binding.removeProductListener = removeProductListener
            binding.namePrice.text = "${item.name}   A$${item.price}"
            binding.quantity.text = "Quantity: "
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentAssignedProductsRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class AssignedProductsDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class AssignedProductListener(val clickListener: (productId: Int) -> Unit) {
    fun onClick(product: Product) = clickListener(product.productId)
}

class RemoveProductListener(val clickListener: (productId: Int) -> Unit) {
    fun onRemoveProduct(product: Product) = clickListener(product.productId)
}