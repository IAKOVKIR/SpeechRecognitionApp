package com.example.audiochatbot.administrator.store_management.recycler_view_adapters

import android.content.Context
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.databinding.FragmentAssignProductsRecyclerViewAdapterBinding

/**
 * A simple [Fragment] subclass.
 */
class AssignProductsRecyclerViewAdapter(private val clickListener: AssignProductListener,
                                        private val addProductListener: AddProductListener
) : ListAdapter<Product,
        AssignProductsRecyclerViewAdapter.ViewHolder>(AssignProductsDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, addProductListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentAssignProductsRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AssignProductListener, addProductListener: AddProductListener, item: Product) {
            binding.product = item
            binding.clickListener = clickListener
            binding.name.text = context.getString(R.string.product_id_name, item.productId, item.name)
            binding.price.text = context.getString(R.string.price_number, item.price)
            binding.addButton.setOnClickListener {
                addProductListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentAssignProductsRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
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

class AddProductListener(val clickListener: (productId: Int) -> Unit) {
    fun onClick(product: Product) = clickListener(product.productId)
}