package com.example.audiochatbot.administrator.user_management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.User
import com.example.audiochatbot.databinding.TextItemViewBinding

class UserManagementFragmentRecyclerViewAdapter(private val clickListener: UserListener) : ListAdapter<User,
        UserManagementFragmentRecyclerViewAdapter.ViewHolder>(
    UserDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: TextItemViewBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: UserListener, item: User) {
            binding.user = item
            binding.clickListener = clickListener
            binding.userName.text = "${item.firstName}   ${item.lastName}"
            if (item.position == 'E')
                binding.userPosition.text = "Employee"
            else if (item.position == 'A')
                binding.userPosition.text = "Administrator"
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TextItemViewBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(
                    binding
                )
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

class UserListener(val clickListener: (userId: Int) -> Unit) {
    fun onClick(user: User) = clickListener(user.userId)
}