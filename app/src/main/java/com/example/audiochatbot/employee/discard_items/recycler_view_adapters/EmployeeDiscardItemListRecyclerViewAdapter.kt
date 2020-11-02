package com.example.audiochatbot.employee.discard_items.recycler_view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.models.DiscardedItem
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.database.models.User
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentEmployeeDiscardItemListRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmployeeDiscardItemListRecyclerViewAdapter(private val userDao: UserDao) : ListAdapter<DiscardedItem,
        EmployeeDiscardItemListRecyclerViewAdapter.ViewHolder>(
    EmployeeDiscardItemListDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item, userDao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentEmployeeDiscardItemListRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(item: DiscardedItem, userDao: UserDao) {
            binding.discardedItemsId.text = "Discarded Items ${item.discardedItemId}"
            binding.quantity.text = "Quantity: ${item.quantity}"
            binding.description.text = "Reasons to discard: ${item.comment}"
            binding.dateTime.text = "${item.date} / ${item.time}"

            CoroutineScope(Dispatchers.Default).launch {
                lateinit var product: Product
                lateinit var user: User
                withContext(Dispatchers.IO) {
                    product = userDao.getProductWithAssignedProductId(item.assignedProductId)
                    user = userDao.getUserWithId(item.userId)
                }

                launch (Dispatchers.Main) {
                    binding.productIdName.text = "${product.productId} / ${product.name}"
                    binding.discardedBy.text = "Discarded by: ${user.userId} / ${user.firstName} ${user.lastName}"
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentEmployeeDiscardItemListRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class EmployeeDiscardItemListDiffCallback : DiffUtil.ItemCallback<DiscardedItem>() {
    override fun areItemsTheSame(oldItem: DiscardedItem, newItem: DiscardedItem): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: DiscardedItem, newItem: DiscardedItem): Boolean {
        return false
    }
}