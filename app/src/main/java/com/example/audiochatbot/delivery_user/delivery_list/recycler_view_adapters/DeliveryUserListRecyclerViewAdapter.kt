package com.example.audiochatbot.delivery_user.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Delivery
import com.example.audiochatbot.databinding.FragmentDeliveryUserListRecyclerViewAdapterBinding

class DeliveryUserListRecyclerViewAdapter(private val clickListener: DeliveryUserListListener,
                                              private val cancelDeliveryListener: DeliveryUserListCancelDeliveryListener,
                                          private val deliveredDeliveryListener: DeliveryUserListDeliveredDeliveryListener
) : ListAdapter<Delivery,
        DeliveryUserListRecyclerViewAdapter.ViewHolder>(
    DeliveryUserListDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, cancelDeliveryListener, deliveredDeliveryListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDeliveryUserListRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: DeliveryUserListListener, cancelDeliveryListener: DeliveryUserListCancelDeliveryListener,
                 deliveredDeliveryListener: DeliveryUserListDeliveredDeliveryListener, item: Delivery) {
            binding.delivery = item
            binding.clickListener = clickListener
            binding.deliveryName.text = "Delivery ${item.deliveryId}"
            binding.status.text = "Status: ${item.status}"

            if (item.status == "Waiting") {
                binding.cancelButton.isEnabled = false
                binding.deliveredButton.isEnabled = false
            }

            binding.cancelButton.setOnClickListener {
                cancelDeliveryListener.onClick(item)
            }

            binding.deliveredButton.setOnClickListener {
                deliveredDeliveryListener.onClick(item)
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDeliveryUserListRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class DeliveryUserListDiffCallback : DiffUtil.ItemCallback<Delivery>() {
    override fun areItemsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem.deliveryId == newItem.deliveryId && oldItem.status == newItem.status
    }

    override fun areContentsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem == newItem
    }
}

class DeliveryUserListListener(val clickListener: (deliveryId: Int) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery.deliveryId)
}

class DeliveryUserListCancelDeliveryListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}

class DeliveryUserListDeliveredDeliveryListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}