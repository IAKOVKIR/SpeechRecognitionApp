package com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.DeliveryProduct
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentDeliveryDetailsRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeliveryDetailsRecyclerViewAdapter(private val deliveryId: Int, private val acceptClickListener: AcceptDeliveryProductsListener,
                                         private val declineClickListener: DeclineDeliveryProductsListener,
                                         private val userDao: UserDao) : ListAdapter<DeliveryProduct,
        DeliveryDetailsRecyclerViewAdapter.ViewHolder>(
    DeliveryProductDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(deliveryId, acceptClickListener, declineClickListener, item, userDao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDeliveryDetailsRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(deliveryId: Int, acceptClickListener: AcceptDeliveryProductsListener,
                 declineClickListener: DeclineDeliveryProductsListener, item: DeliveryProduct,
                 userDao: UserDao) {
            if (item.status != "not available") {
                binding.status.text = item.status
                binding.status.visibility = View.VISIBLE
            } else {
                binding.acceptButton.visibility = View.VISIBLE
                binding.declineButton.visibility = View.VISIBLE
            }

            CoroutineScope(Dispatchers.Default).launch {

                var productId: Int
                var productName: String
                var smallUnitName: String
                var bigUnitName: String
                var status: String

                withContext(Dispatchers.IO) {
                    val obj = userDao.getProductIdWithAssignedProductId(item.assignedProductId)
                    productId = obj.productId
                    productName = obj.name
                    smallUnitName = obj.smallUnitName
                    bigUnitName = obj.bigUnitName
                    status = userDao.getDeliveryStatus(deliveryId)
                }

                launch (Dispatchers.Main) {
                    binding.itemSet.text = "Item set ${item.assignedProductId}"
                    binding.productName.text = productName
                    binding.smallUnitName.text = "$smallUnitName: ${item.smallUnitQuantity}"
                    binding.bigUnitName.text = "$bigUnitName: ${item.bigUnitQuantity}"
                    if (status == "Delivered") {
                        binding.acceptButton.isEnabled = true
                        binding.declineButton.isEnabled = true
                    }
                }
            }

            binding.acceptButton.setOnClickListener {
                acceptClickListener.onClick(item)
                binding.acceptButton.visibility = View.GONE
                binding.declineButton.visibility = View.GONE

                binding.status.text = "accepted"
                binding.status.visibility = View.VISIBLE
            }

            binding.declineButton.setOnClickListener {
                declineClickListener.onClick(item)
                binding.acceptButton.visibility = View.GONE
                binding.declineButton.visibility = View.GONE

                binding.status.text = "declined"
                binding.status.visibility = View.VISIBLE
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDeliveryDetailsRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class DeliveryProductDiffCallback : DiffUtil.ItemCallback<DeliveryProduct>() {
    override fun areItemsTheSame(oldItem: DeliveryProduct, newItem: DeliveryProduct): Boolean {
        return oldItem.deliveryId == newItem.deliveryId && oldItem.assignedProductId == newItem.assignedProductId
    }

    override fun areContentsTheSame(oldItem: DeliveryProduct, newItem: DeliveryProduct): Boolean {
        return oldItem == newItem
    }
}

class AcceptDeliveryProductsListener(val clickListener: (deliveryProduct: DeliveryProduct) -> Unit) {
    fun onClick(deliveryProduct: DeliveryProduct) = clickListener(deliveryProduct)
}

class DeclineDeliveryProductsListener(val clickListener: (deliveryProduct: DeliveryProduct) -> Unit) {
    fun onClick(deliveryProduct: DeliveryProduct) = clickListener(deliveryProduct)
}