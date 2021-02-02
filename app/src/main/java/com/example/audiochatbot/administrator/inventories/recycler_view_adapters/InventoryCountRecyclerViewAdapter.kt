package com.example.audiochatbot.administrator.inventories.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.databinding.FragmentInventoryCountRecyclerViewAdapterBinding

class InventoryCountRecyclerViewAdapter(private val addInventoryListener: AddInventoryListener,
                                        private val removeInventoryListener: RemoveInventoryListener,
                                        private val list: List<Int>
) : ListAdapter<Product, InventoryCountRecyclerViewAdapter.ViewHolder>(
    InventoryCountDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(addInventoryListener, removeInventoryListener, item, list[position * 2], list[position * 2 + 1])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentInventoryCountRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(addInventoryListener: AddInventoryListener,
                 removeInventoryListener: RemoveInventoryListener,
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

            } else if (big != 0) {
                binding.bigQuantity.setText("$big", TextView.BufferType.EDITABLE)
                option = false
                binding.addRemoveButton.text = "remove"
            }

            binding.addRemoveButton.setOnClickListener {
                if (option) {
                    val smallNum = binding.smallQuantity.text.toString()
                    val bigNum = binding.bigQuantity.text.toString()

                    when {
                        smallNum != "" -> {
                            option = if (bigNum == "") {
                                addInventoryListener.onClick(item, smallNum.toInt(), 0)

                                smallNum.toInt() == 0
                            } else {
                                addInventoryListener.onClick(
                                    item,
                                    smallNum.toInt(),
                                    bigNum.toInt()
                                )

                                bigNum.toInt() == 0 && smallNum.toInt() == 0
                            }
                        }
                        bigNum != "" -> {
                            addInventoryListener.onClick(item, 0, bigNum.toInt())

                            option = bigNum.toInt() == 0
                        }
                        else -> addInventoryListener.onClick(item, 0, 0)
                    }

                } else
                    removeInventoryListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentInventoryCountRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class InventoryCountDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class AddInventoryListener(val clickListener: (product: Product, smallQuantity: Int, bigQuantity: Int) -> Unit) {
    fun onClick(product: Product, smallQuantity: Int, bigQuantity: Int) = clickListener(product, smallQuantity, bigQuantity)
}

class RemoveInventoryListener(val clickListener: (product: Product) -> Unit) {
    fun onClick(product: Product) = clickListener(product)
}