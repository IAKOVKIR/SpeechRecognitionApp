package com.example.audiochatbot.employee.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.database.models.Delivery
import com.example.audiochatbot.database.models.DeliveryProductStatus
import com.example.audiochatbot.databinding.FragmentEmployeeDeliveryListRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmployeeDeliveryListRecyclerViewAdapter(private val userDao: UserDao,
                                              private val clickListener: EmployeeDeliveryListener,
                                              private val deliveredListener: EmployeeDeliveredDeliveryListener
) : ListAdapter<Delivery,
        EmployeeDeliveryListRecyclerViewAdapter.ViewHolder>(
    EmployeeDeliveryDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, deliveredListener, item, userDao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentEmployeeDeliveryListRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: EmployeeDeliveryListener, deliveredListener: EmployeeDeliveredDeliveryListener,
                 item: Delivery, userDao: UserDao) {
            binding.delivery = item
            binding.clickListener = clickListener
            binding.deliveryName.text = "Delivery ${item.deliveryId}"
            binding.createdBy.text = "Created by: User ${item.userId}"

            if (item.status == "In Transit") {
                binding.status.text = "Status: ${item.status}"
                binding.deliveredButton.isEnabled = true
            } else {
                CoroutineScope(Dispatchers.Default).launch {

                    var line: String

                    withContext(Dispatchers.IO) {
                        val deliveryProductStatus = userDao.getFirstDeliveryProductStatus(item.deliveryId)
                        line = if (deliveryProductStatus == null) {
                            "Status: ${item.status} by User ${item.userId}"
                        } else {
                            "Status: ${item.status} by User ${deliveryProductStatus.userId}"
                        }
                    }

                    launch (Dispatchers.Main) {
                        binding.status.text = line
                    }
                }
                binding.deliveredButton.isEnabled = false
            }

            binding.deliveredButton.setOnClickListener {
                deliveredListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentEmployeeDeliveryListRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class EmployeeDeliveryDiffCallback : DiffUtil.ItemCallback<Delivery>() {
    override fun areItemsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem.deliveryId == newItem.deliveryId && oldItem.status == newItem.status
    }

    override fun areContentsTheSame(oldItem: Delivery, newItem: Delivery): Boolean {
        return oldItem == newItem
    }
}

class EmployeeDeliveryListener(val clickListener: (deliveryId: Int) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery.deliveryId)
}

class EmployeeDeliveredDeliveryListener(val clickListener: (delivery: Delivery) -> Unit) {
    fun onClick(delivery: Delivery) = clickListener(delivery)
}