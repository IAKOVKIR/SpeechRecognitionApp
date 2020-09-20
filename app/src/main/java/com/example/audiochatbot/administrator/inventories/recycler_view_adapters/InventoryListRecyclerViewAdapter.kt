package com.example.audiochatbot.administrator.inventories.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.InventoryCount
import com.example.audiochatbot.databinding.FragmentInventoryListRecyclerViewAdapterBinding

class InventoryListRecyclerViewAdapter : ListAdapter<InventoryCount,
        InventoryListRecyclerViewAdapter.ViewHolder>(
    InventoryCountDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentInventoryListRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(item: InventoryCount) {
            binding.deliveryName.text = "Store: ${item.storeId}"
            binding.earnings.text = "Expected: ${item.expectedEarnings} / Earned: ${item.totalEarnings}"
            binding.countedBy.text = "Counted by: User ${item.userId}"
            binding.dateTime.text = "${item.date} / ${item.time}"
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentInventoryListRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class InventoryCountDiffCallback : DiffUtil.ItemCallback<InventoryCount>() {
    override fun areItemsTheSame(oldItem: InventoryCount, newItem: InventoryCount): Boolean {
        return oldItem.userId == newItem.userId && oldItem.storeId == newItem.storeId
    }

    override fun areContentsTheSame(oldItem: InventoryCount, newItem: InventoryCount): Boolean {
        return oldItem.equals(newItem)
    }
}