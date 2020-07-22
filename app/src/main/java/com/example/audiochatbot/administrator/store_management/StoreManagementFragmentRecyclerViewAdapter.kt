package com.example.audiochatbot.administrator.store_management

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.databinding.StoreManagementItemViewBinding

class StoreManagementFragmentRecyclerViewAdapter(private val clickListener: StoreListener) : ListAdapter<Store,
        StoreManagementFragmentRecyclerViewAdapter.ViewHolder>(
    UserDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: StoreManagementItemViewBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: StoreListener, item: Store) {
            binding.store = item
            binding.clickListener = clickListener
            binding.storeName.text = "Store ${item.storeId} / Business ${item.businessId}"
            binding.storeAddress.text = "${item.street}, ${item.city}"
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StoreManagementItemViewBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(
                    binding
                )
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<Store>() {
    override fun areItemsTheSame(oldItem: Store, newItem: Store): Boolean {
        return oldItem.storeId == newItem.storeId
    }

    override fun areContentsTheSame(oldItem: Store, newItem: Store): Boolean {
        return oldItem == newItem
    }
}

class StoreListener(val clickListener: (storeId: Int) -> Unit) {
    fun onClick(store: Store) = clickListener(store.storeId)
}