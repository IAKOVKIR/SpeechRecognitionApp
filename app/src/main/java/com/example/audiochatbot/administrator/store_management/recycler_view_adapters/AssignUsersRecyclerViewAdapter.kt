package com.example.audiochatbot.administrator.store_management.recycler_view_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.User
import com.example.audiochatbot.databinding.FragmentAssignUsersRecyclerViewAdapterBinding

class AssignUsersRecyclerViewAdapter(private val clickListener: AssignedUserListener,
                                     private val addUserListener: AddUserListener
) : ListAdapter<User,
        AssignUsersRecyclerViewAdapter.ViewHolder>(
    AssignUsersDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, addUserListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: FragmentAssignUsersRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AssignedUserListener, addUserListener: AddUserListener, item: User) {
            binding.user = item
            binding.clickListener = clickListener
            binding.addUserListener = addUserListener
            binding.userName.text = "${item.firstName}   ${item.lastName}"
            if (item.position == 'E')
                binding.userPosition.text = "Employee"
            else if (item.position == 'A')
                binding.userPosition.text = "Administrator"

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentAssignUsersRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(
                    binding
                )
            }
        }
    }
}

class AssignUsersDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

class AssignedUserListener(private val clickListener: (userId: Int) -> Unit) {
    fun onClick(user: User) = clickListener(user.userId)
}

class AddUserListener(private val clickListener: (userId: Int) -> Unit) {
    fun onAddUser(user: User) = clickListener(user.userId)
}