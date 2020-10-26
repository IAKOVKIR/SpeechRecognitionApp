package com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Delivery
import com.example.audiochatbot.databinding.FragmentDeliveryListRecyclerViewAdapterBinding

class DeliveryListRecyclerViewAdapter(private val adminId: Int, private val clickListener: DeliveryListener,
                                      private val cancelDeliveryListener: CancelDeliveryListener
) : ListAdapter<Delivery,
        DeliveryListRecyclerViewAdapter.ViewHolder>(
    DeliveryDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(adminId, clickListener, cancelDeliveryListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDeliveryListRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(adminId: Int, clickListener: DeliveryListener, cancelDeliveryListener: CancelDeliveryListener, item: Delivery) {
            binding.delivery = item
            binding.clickListener = clickListener
            binding.deliveryName.text = "Delivery ${item.deliveryId}"
            binding.status.text = "Status: ${item.status}"

            if (item.status == "Delivered" || item.status == "Canceled") {
                binding.cancelButton.isEnabled = false
                binding.deliveredButton.isEnabled = false
            } else if (item.userId != adminId)
                binding.deliveredButton.isEnabled = false

            binding.cancelButton.setOnClickListener {
                cancelDeliveryListener.onClick(item)
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDeliveryListRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class DeliveryDiffCallback : DiffUtil.ItemCallback<Delivery>() {
    override fun areItemsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem.deliveryId == newItem.deliveryId && oldItem.status == newItem.status
    }

    override fun areContentsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem == newItem
    }
}

class DeliveryListener(val clickListener: (deliveryId: Int) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery.deliveryId)
}

class CancelDeliveryListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}