package com.example.audiochatbot.delivery_user.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.DeliveryProduct
import com.example.audiochatbot.database.Product
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentDeliveryUserListDetailsRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeliveryUserListDetailsRecyclerViewAdapter(private val userDao: UserDao) : ListAdapter<DeliveryProduct,
        DeliveryUserListDetailsRecyclerViewAdapter.ViewHolder>(
    DeliveryUserListProductDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item, userDao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDeliveryUserListDetailsRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(item: DeliveryProduct, userDao: UserDao) {
            CoroutineScope(Dispatchers.Default).launch {

                lateinit var obj: Product

                withContext(Dispatchers.IO) {
                    obj = userDao.getProductWithAssignedProductId(item.assignedProductId)
                }

                launch (Dispatchers.Main) {
                    binding.itemSet.text = "Item set ${item.assignedProductId}"
                    binding.productName.text = obj.name
                    binding.smallUnitName.text = "${obj.smallUnitName}: ${item.smallUnitQuantity}"
                    binding.bigUnitName.text = "${obj.bigUnitName}: ${item.bigUnitQuantity}"
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDeliveryUserListDetailsRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class DeliveryUserListProductDiffCallback : DiffUtil.ItemCallback<DeliveryProduct>() {
    override fun areItemsTheSame(oldItem: DeliveryProduct, newItem: DeliveryProduct): Boolean {
        return oldItem.deliveryId == newItem.deliveryId && oldItem.assignedProductId == newItem.assignedProductId
    }

    override fun areContentsTheSame(oldItem: DeliveryProduct, newItem: DeliveryProduct): Boolean {
        return oldItem == newItem
    }
}