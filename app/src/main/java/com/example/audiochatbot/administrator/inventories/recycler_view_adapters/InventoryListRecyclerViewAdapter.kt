package com.example.audiochatbot.administrator.inventories.recycler_view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.models.InventoryCount
import com.example.audiochatbot.databinding.FragmentInventoryListRecyclerViewAdapterBinding

class InventoryListRecyclerViewAdapter(private val clickListener: DownloadTheReportListener) : ListAdapter<InventoryCount,
        InventoryListRecyclerViewAdapter.ViewHolder>(
    InventoryListDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentInventoryListRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: DownloadTheReportListener, item: InventoryCount) {
            binding.deliveryName.text = "Inventory Count: ${item.inventoryCountId} / Store: ${item.storeId}"
            binding.earnings.text = context.getString(R.string.expected_earned, item.expectedEarnings, item.totalEarnings)
            binding.difference.text = context.getString(R.string.difference, item.expectedEarnings - item.totalEarnings)
            binding.countedBy.text = "Counted by: User ${item.userId}"
            binding.dateTime.text = "${item.date} / ${item.time}"
            binding.inventoryCount = item
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentInventoryListRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class InventoryListDiffCallback : DiffUtil.ItemCallback<InventoryCount>() {
    override fun areItemsTheSame(oldItem: InventoryCount, newItem: InventoryCount): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: InventoryCount, newItem: InventoryCount): Boolean {
        return false
    }
}

class DownloadTheReportListener(val clickListener: (inventoryCount: InventoryCount) -> Unit) {
    fun onClick(inventoryCount: InventoryCount) = clickListener(inventoryCount)
}