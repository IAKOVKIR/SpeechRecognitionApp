package com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.models.Delivery
import com.example.audiochatbot.databinding.FragmentDeliveryListRecyclerViewAdapterBinding

class DeliveryListRecyclerViewAdapter(private val clickListener: DeliveryListener,
                                      private val cancelDeliveryListener: CancelDeliveryListener,
                                      private val adminDeliveredListener: AdminDeliveredListener
) : ListAdapter<Delivery,
        DeliveryListRecyclerViewAdapter.ViewHolder>(
    DeliveryDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, cancelDeliveryListener, adminDeliveredListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDeliveryListRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: DeliveryListener, cancelDeliveryListener: CancelDeliveryListener,
                 adminDeliveredListener: AdminDeliveredListener, item: Delivery
        ) {
            binding.delivery = item
            binding.clickListener = clickListener
            binding.deliveryName.text = "Delivery ${item.deliveryId}"
            binding.status.text = "Status: ${item.status}"

            when {
                item.status != "In Transit" -> {
                    binding.cancelButton.isEnabled = false
                    binding.deliveredButton.isEnabled = false
                }
                else -> {
                    binding.cancelButton.isEnabled = true
                    binding.deliveredButton.isEnabled = true
                }
            }

            binding.cancelButton.setOnClickListener {
                cancelDeliveryListener.onClick(item)
            }

            binding.deliveredButton.setOnClickListener {
                adminDeliveredListener.onClick(item)
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

class DeliveryListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}

class CancelDeliveryListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}

class AdminDeliveredListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}