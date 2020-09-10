package com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.databinding.FragmentCreateDeliveryRecyclerViewAdapterBinding

class CreateDeliveryRecyclerViewAdapter(private val addDeliveryProductListener: AddDeliveryProductListener
) : ListAdapter<Product, CreateDeliveryRecyclerViewAdapter.ViewHolder>(
    CreateDeliveryDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(addDeliveryProductListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentCreateDeliveryRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(addDeliveryProductListener: AddDeliveryProductListener,
                 item: Product) {
            binding.productName.text = item.name
            binding.smallUnitName.text = item.smallUnitName
            binding.bigUnitName.text = item.bigUnitName
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentCreateDeliveryRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class CreateDeliveryDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class AddDeliveryProductListener(val clickListener: (product: Product) -> Unit) {
    fun onClick(product: Product) = clickListener(product)
}