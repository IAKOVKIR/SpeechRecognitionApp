package com.example.audiochatbot.administrator.store_management.recycler_view_adapters

import android.content.Context
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.models.AssignedProduct
import com.example.audiochatbot.database.models.Product
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentAssignedProductsRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 */
class AssignedProductsRecyclerViewAdapter(private val clickListener: AssignedProductListener,
                                          private val removeProductListener: RemoveProductListener,
                                          private val storeId: Int,
                                          private val database: UserDao
) : ListAdapter<Product,
        AssignedProductsRecyclerViewAdapter.ViewHolder>(
    AssignedProductsDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, removeProductListener, item, database, storeId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentAssignedProductsRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AssignedProductListener, removeProductListener: RemoveProductListener, item: Product, database: UserDao, storeId: Int) {
            binding.product = item
            binding.clickListener = clickListener
            binding.removeProductListener = removeProductListener
            CoroutineScope(Dispatchers.Default).launch {

                var obj: AssignedProduct

                withContext(Dispatchers.IO) {
                    obj = database.getAssignedProduct(item.productId, storeId)!!
                }

                launch (Dispatchers.Main) {
                    binding.quantity.text = context.getString(R.string.total_product_quantity, obj.quantity)
                    if (obj.sale > 0) {
                        binding.namePrice.text = context.getString(R.string.name_price, item.productId, item.name, (item.price * (1.0F - (obj.sale / 100.0F))))
                        binding.namePrice.setTextColor(Color.RED)
                    } else
                        binding.namePrice.text = context.getString(R.string.name_price, item.productId, item.name, item.price)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentAssignedProductsRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class AssignedProductsDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class AssignedProductListener(val clickListener: (productId: Int) -> Unit) {
    fun onClick(product: Product) = clickListener(product.productId)
}

class RemoveProductListener(val clickListener: (productId: Int) -> Unit) {
    fun onRemoveProduct(product: Product) = clickListener(product.productId)
}