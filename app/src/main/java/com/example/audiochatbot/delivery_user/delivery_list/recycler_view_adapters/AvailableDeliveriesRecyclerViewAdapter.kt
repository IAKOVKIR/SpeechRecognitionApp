package com.example.audiochatbot.delivery_user.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Delivery
import com.example.audiochatbot.databinding.FragmentAvailableDeliveriesRecyclerViewAdapterBinding

class AvailableDeliveriesRecyclerViewAdapter(private val clickListener: AvailableDeliveryListener,
                                          private val deliverDeliveryListener: DeliverDeliveryListener
) : ListAdapter<Delivery,
        AvailableDeliveriesRecyclerViewAdapter.ViewHolder>(
    AvailableDeliveriesDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, deliverDeliveryListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentAvailableDeliveriesRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AvailableDeliveryListener, deliverDeliveryListener: DeliverDeliveryListener,
                 item: Delivery) {
            binding.delivery = item
            binding.clickListener = clickListener
            binding.deliveryName.text = "Delivery ${item.deliveryId}"
            binding.status.text = "Status: ${item.status}"

            if (item.status != "Waiting") {
                binding.deliverButton.isEnabled = false
            }

            binding.deliverButton.setOnClickListener {
                deliverDeliveryListener.onClick(item)
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentAvailableDeliveriesRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class AvailableDeliveriesDiffCallback : DiffUtil.ItemCallback<Delivery>() {
    override fun areItemsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem.deliveryId == newItem.deliveryId && oldItem.status == newItem.status
    }

    override fun areContentsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem == newItem
    }
}

class AvailableDeliveryListener(val clickListener: (deliveryId: Int) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery.deliveryId)
}

class DeliverDeliveryListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}