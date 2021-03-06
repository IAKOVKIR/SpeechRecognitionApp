package com.example.audiochatbot.employee.inventories.recycler_view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.models.InventoryCount
import com.example.audiochatbot.databinding.FragmentEmployeeInventoryListRecyclerViewAdapterBinding

class EmployeeInventoryListRecyclerViewAdapter : ListAdapter<InventoryCount,
        EmployeeInventoryListRecyclerViewAdapter.ViewHolder>(
    EmployeeInventoryListDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentEmployeeInventoryListRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(item: InventoryCount) {
            binding.deliveryName.text = "Store: ${item.storeId}"
            binding.earnings.text = context.getString(R.string.expected_earned, item.expectedEarnings, item.totalEarnings)
            binding.difference.text = context.getString(R.string.difference, item.expectedEarnings - item.totalEarnings)
            binding.countedBy.text = "Counted by: User ${item.userId}"
            binding.dateTime.text = "${item.date} / ${item.time}"
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentEmployeeInventoryListRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class EmployeeInventoryListDiffCallback : DiffUtil.ItemCallback<InventoryCount>() {
    override fun areItemsTheSame(oldItem: InventoryCount, newItem: InventoryCount): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: InventoryCount, newItem: InventoryCount): Boolean {
        return false
    }
}