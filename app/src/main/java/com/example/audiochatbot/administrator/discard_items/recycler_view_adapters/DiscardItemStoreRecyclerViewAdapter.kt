package com.example.audiochatbot.administrator.discard_items.recycler_view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.databinding.FragmentDiscardItemStoreRecyclerViewAdapterBinding

class DiscardItemStoreRecyclerViewAdapter(private val clickListener: ItemStoreListener) : ListAdapter<Store,
        DiscardItemStoreRecyclerViewAdapter.ViewHolder>(
    ItemStoreDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDiscardItemStoreRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: ItemStoreListener, item: Store) {
            binding.store = item
            binding.clickListener = clickListener
            binding.storeName.text = context.getString(R.string.store_business, item.storeId, item.businessId)
            binding.storeAddress.text = context.getString(R.string.comma, item.street, item.city)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDiscardItemStoreRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class ItemStoreDiffCallback : DiffUtil.ItemCallback<Store>() {
    override fun areItemsTheSame(oldItem: Store, newItem: Store): Boolean {
        return oldItem.storeId == newItem.storeId
    }

    override fun areContentsTheSame(oldItem: Store, newItem: Store): Boolean {
        return oldItem == newItem
    }
}

class ItemStoreListener(val clickListener: (storeId: Int) -> Unit) {
    fun onClick(store: Store) = clickListener(store.storeId)
}