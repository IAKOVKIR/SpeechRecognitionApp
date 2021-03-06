package com.example.audiochatbot.administrator.delivery_list.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.databinding.FragmentCreateDeliveryRecyclerViewAdapterBinding

class CreateDeliveryRecyclerViewAdapter(private val addDeliveryProductListener: AddDeliveryProductListener,
                                        private val removeDeliveryProductListener: RemoveDeliveryProductListener,
                                        private val list: List<Int>
) : ListAdapter<Product, CreateDeliveryRecyclerViewAdapter.ViewHolder>(
    CreateDeliveryDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(addDeliveryProductListener, removeDeliveryProductListener, item, list[position * 2], list[position * 2 + 1])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentCreateDeliveryRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(addDeliveryProductListener: AddDeliveryProductListener,
                 removeDeliveryProductListener: RemoveDeliveryProductListener,
                 item: Product, small: Int, big: Int) {
            var option = true
            binding.productName.text = item.name
            binding.smallUnitName.text = item.smallUnitName
            binding.bigUnitName.text = item.bigUnitName

            if (small != 0) {
                if (big != 0)
                    binding.bigQuantity.setText("$big", TextView.BufferType.EDITABLE)

                binding.smallQuantity.setText("$small", TextView.BufferType.EDITABLE)
                option = false
                binding.addRemoveButton.text = "remove"

            } else {
                if (big != 0) {
                    binding.bigQuantity.setText("$big", TextView.BufferType.EDITABLE)
                    option = false
                    binding.addRemoveButton.text = "remove"
                }
            }

            binding.addRemoveButton.setOnClickListener {
                if (option) {
                    val smallNum = binding.smallQuantity.text.toString()
                    val bigNum = binding.bigQuantity.text.toString()

                    if (smallNum != "") {
                        if (bigNum == "") {
                            addDeliveryProductListener.onClick(item, smallNum.toInt(), 0)

                            if (smallNum.toInt() != 0)
                                option = false
                        } else {
                            addDeliveryProductListener.onClick(
                                item,
                                smallNum.toInt(),
                                bigNum.toInt()
                            )

                            if (bigNum.toInt() != 0 || smallNum.toInt() != 0)
                                option = false
                        }
                    } else if (bigNum != "") {
                        addDeliveryProductListener.onClick(item, 0, bigNum.toInt())

                        if (bigNum.toInt() != 0)
                            option = false
                    } else {
                        addDeliveryProductListener.onClick(item, 0, 0)
                    }

                } else
                    removeDeliveryProductListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentCreateDeliveryRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class CreateDeliveryDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class AddDeliveryProductListener(val clickListener: (product: Product, smallQuantity: Int, bigQuantity: Int) -> Unit) {
    fun onClick(product: Product, smallQuantity: Int, bigQuantity: Int) = clickListener(product, smallQuantity, bigQuantity)
}

class RemoveDeliveryProductListener(val clickListener: (product: Product) -> Unit) {
    fun onClick(product: Product) = clickListener(product)
}