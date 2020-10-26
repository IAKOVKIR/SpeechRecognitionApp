package com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.DeliveryProduct
import com.example.audiochatbot.database.DeliveryProductStatus
import com.example.audiochatbot.database.Product
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
            CoroutineScope(Dispatchers.Default).launch {

                var status: String
                lateinit var obj: Product
                var obj2: DeliveryProductStatus?

                withContext(Dispatchers.IO) {
                    obj = userDao.getProductWithAssignedProductId(item.assignedProductId)
                    obj2 = userDao.getDeliveryProductStatus(item.deliveryProductId)
                    status = userDao.getDeliveryStatus(deliveryId)
                }

                launch (Dispatchers.Main) {
                    binding.itemSet.text = "Item set ${item.assignedProductId}"
                    binding.productName.text = obj.name
                    binding.smallUnitName.text = "${obj.smallUnitName}: ${item.smallUnitQuantity}"
                    binding.bigUnitName.text = "${obj.bigUnitName}: ${item.bigUnitQuantity}"

                    if (status == "Delivered" && obj2 != null) {
                        binding.status.text = "${obj2!!.status} by User ${obj2!!.userId}"
                        binding.status.visibility = View.VISIBLE
                        binding.acceptButton.visibility = View.GONE
                        binding.declineButton.visibility = View.GONE
                        binding.acceptButton.isEnabled = false
                        binding.declineButton.isEnabled = false
                    } else {
                        binding.status.visibility = View.GONE
                        binding.acceptButton.visibility = View.VISIBLE
                        binding.declineButton.visibility = View.VISIBLE
                        if (status == "Delivered") {
                            binding.acceptButton.isEnabled = true
                            binding.declineButton.isEnabled = true
                        } else {
                            binding.acceptButton.isEnabled = false
                            binding.declineButton.isEnabled = false
                        }
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