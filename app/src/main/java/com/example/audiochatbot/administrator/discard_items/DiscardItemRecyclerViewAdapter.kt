package com.example.audiochatbot.administrator.discard_items

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.databinding.FragmentDiscardItemRecyclerViewAdapterBinding

/**
 * A simple [Fragment] subclass.
 */
class DiscardItemRecyclerViewAdapter(private val discardProductListener: DiscardProductListener
) : ListAdapter<Product,
        DiscardItemRecyclerViewAdapter.ViewHolder>(DiscardItemsDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(discardProductListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDiscardItemRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(discardProductListener: DiscardProductListener, item: Product) {
            binding.name.text = item.name
            binding.price.text = "A$${item.price}"
            binding.addButton.setOnClickListener {
                val num = binding.quantity.text.toString()
                if (num != "") {
                    discardProductListener.onClick(item, num.toInt())
                } else {
                    discardProductListener.onClick(item, 0)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDiscardItemRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class DiscardItemsDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class DiscardProductListener(val clickListener: (productId: Int, quantity: Int) -> Unit) {
    fun onClick(product: Product, quantity: Int) = clickListener(product.productId, quantity)
}