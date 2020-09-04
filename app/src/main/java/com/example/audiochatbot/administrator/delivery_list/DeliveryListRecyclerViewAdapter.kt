package com.example.audiochatbot.administrator.delivery_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Delivery
import com.example.audiochatbot.databinding.FragmentDeliveryListRecyclerViewAdapterBinding

class DeliveryListRecyclerViewAdapter(/*private val clickListener: StoreListener*/) : ListAdapter<Delivery,
        DeliveryListRecyclerViewAdapter.ViewHolder>(
    DeliveryDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(/*clickListener,*/ item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: FragmentDeliveryListRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(/*clickListener: StoreListener,*/ item: Delivery) {
            binding.delivery = item
            //binding.clickListener = clickListener
            binding.deliveryName.text = "Delivery ${item.deliveryId} / Store ${item.storeId}"
            binding.status.text = "Status: ${item.status}"
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
        return oldItem.deliveryId == newItem.deliveryId
    }

    override fun areContentsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem == newItem
    }
}

//class StoreListener(val clickListener: (storeId: Int) -> Unit) {
  //  fun onClick(store: Store) = clickListener(store.storeId)
//}