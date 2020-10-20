package com.example.audiochatbot.employee.cash_report.recycler_view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.Store
import com.example.audiochatbot.databinding.FragmentSelectStoreRecyclerViewAdapterBinding

class SelectStoreRecyclerViewAdapter(private val clickListener: SelectStoreListener) : ListAdapter<Store,
        SelectStoreRecyclerViewAdapter.ViewHolder>(
    SelectStoreDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentSelectStoreRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: SelectStoreListener, item: Store) {
            binding.store = item
            binding.clickListener = clickListener
            binding.storeName.text = context.getString(R.string.store_business, item.storeId, item.businessId)
            binding.storeAddress.text = context.getString(R.string.comma, item.street, item.city)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentSelectStoreRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class SelectStoreDiffCallback : DiffUtil.ItemCallback<Store>() {
    override fun areItemsTheSame(oldItem: Store, newItem: Store): Boolean {
        return oldItem.storeId == newItem.storeId
    }

    override fun areContentsTheSame(oldItem: Store, newItem: Store): Boolean {
        return oldItem == newItem
    }
}

class SelectStoreListener(val clickListener: (storeId: Int) -> Unit) {
    fun onClick(store: Store) = clickListener(store.storeId)
}