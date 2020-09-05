package com.example.audiochatbot.administrator.delivery_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Delivery
import com.example.audiochatbot.database.DeliveryProduct
import com.example.audiochatbot.databinding.FragmentDeliveryDetailsRecyclerViewAdapterBinding

class DeliveryDetailsRecyclerViewAdapter(private val acceptClickListener: AcceptDeliveryProductsListener,
                                         private val declineClickListener: DeclineDeliveryProductsListener) : ListAdapter<DeliveryProduct,
        DeliveryDetailsRecyclerViewAdapter.ViewHolder>(
    DeliveryProductDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(acceptClickListener, declineClickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: FragmentDeliveryDetailsRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(acceptClickListener: AcceptDeliveryProductsListener, declineClickListener: DeclineDeliveryProductsListener, item: DeliveryProduct) {
            binding.deliveryProduct = item
            binding.acceptClickListener = acceptClickListener
            binding.declineClickListener = declineClickListener
            //binding.deliveryName.text = "Delivery ${item.deliveryId} / Store ${item.storeId}"
            //binding.status.text = "Status: ${item.status}"
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDeliveryDetailsRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class DeliveryProductDiffCallback : DiffUtil.ItemCallback<DeliveryProduct>() {
    override fun areItemsTheSame(oldItem: DeliveryProduct, newItem: DeliveryProduct): Boolean {
        return oldItem.deliveryId == newItem.deliveryId
    }

    override fun areContentsTheSame(oldItem: DeliveryProduct, newItem: DeliveryProduct): Boolean {
        return oldItem == newItem
    }
}

class AcceptDeliveryProductsListener(val clickListener: (deliveryId: Int) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery.deliveryId)
}

class DeclineDeliveryProductsListener(val clickListener: (deliveryId: Int) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery.deliveryId)
}