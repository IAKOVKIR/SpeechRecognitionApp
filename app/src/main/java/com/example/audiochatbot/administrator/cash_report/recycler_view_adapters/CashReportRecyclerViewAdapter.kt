package com.example.audiochatbot.administrator.cash_report.recycler_view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
import com.example.audiochatbot.database.models.CashOperation
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentCashReportRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CashReportRecyclerViewAdapter(private val database: UserDao,
                                    private val clickListener: DownloadTheCashReportListener
) : ListAdapter<CashOperation,
        CashReportRecyclerViewAdapter.ViewHolder>(
    CashOperationDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item, database)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentCashReportRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: DownloadTheCashReportListener, item: CashOperation, database: UserDao) {
            binding.cashReport = item
            binding.clickListener = clickListener
            CoroutineScope(Dispatchers.Default).launch {

                var fullName: String

                withContext(Dispatchers.Default) {
                    val user = database.getUserWithId(item.userId)
                    fullName = "User ${item.userId}, ${user.firstName} ${user.lastName}"
                }

                launch (Dispatchers.Main) {
                    binding.userName.text = fullName
                }
            }

            if (item.operationType)
                binding.operationAmount.text = context.getString(R.string.deposited_amount, item.amount)
            else
                binding.operationAmount.text = context.getString(R.string.withdrawn_amount, item.amount)
            binding.dateTime.text = context.getString(R.string.comma, item.date, item.time)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentCashReportRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
            }
        }
    }
}

class CashOperationDiffCallback : DiffUtil.ItemCallback<CashOperation>() {
    override fun areItemsTheSame(oldItem: CashOperation, newItem: CashOperation): Boolean {
        return oldItem.cashOperationId == newItem.cashOperationId
    }

    override fun areContentsTheSame(oldItem: CashOperation, newItem: CashOperation): Boolean {
        return oldItem == newItem
    }
}

class DownloadTheCashReportListener(val clickListener: (cashOperation: CashOperation) -> Unit) {
    fun onClick(cashOperation: CashOperation) = clickListener(cashOperation)
}