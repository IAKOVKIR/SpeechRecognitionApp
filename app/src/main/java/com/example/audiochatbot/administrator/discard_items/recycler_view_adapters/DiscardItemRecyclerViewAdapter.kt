package com.example.audiochatbot.administrator.discard_items.recycler_view_adapters

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentDiscardItemRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 */
class DiscardItemRecyclerViewAdapter(private val storeId: Int, private val discardProductListener: DiscardProductListener,
                                     private val userDao: UserDao
) : ListAdapter<Product,
        DiscardItemRecyclerViewAdapter.ViewHolder>(DiscardItemsDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(storeId, discardProductListener, item, userDao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentDiscardItemRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(storeId: Int, discardProductListener: DiscardProductListener, item: Product, userDao: UserDao) {
            binding.name.text = "${item.productId} / ${item.name}"
            binding.price.text = "A$${item.price}"

            CoroutineScope(Dispatchers.Default).launch {
                var num: Int

                withContext(Dispatchers.IO) {
                    num = userDao.getQuantity(item.productId, storeId)
                }

                launch (Dispatchers.Main) {
                    binding.quantityOfTheItem.text = "Quantity: $num"
                }
            }

            binding.addButton.setOnClickListener {
                val num = binding.quantity.text.toString()
                val comment = binding.comment.text.toString()
                if (num != "") {
                    discardProductListener.onClick(item, num.toInt(), comment)
                } else {
                    discardProductListener.onClick(item, 0, comment)
                }
                binding.quantity.text = null
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentDiscardItemRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class DiscardItemsDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class DiscardProductListener(val clickListener: (productId: Int, quantity: Int, comment: String) -> Unit) {
    fun onClick(product: Product, quantity: Int, comment: String) = clickListener(product.productId, quantity, comment)
}