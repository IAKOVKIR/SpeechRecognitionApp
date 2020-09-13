package com.example.audiochatbot.administrator.store_management.recycler_view_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.R
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

    class ViewHolder private constructor(val binding: FragmentAssignUsersRecyclerViewAdapterBinding, val context: Context)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AssignedUserListener, addUserListener: AddUserListener, item: User) {
            binding.user = item
            binding.clickListener = clickListener
            binding.addUserListener = addUserListener
            binding.userName.text = context.getString(R.string.first_last_names, item.firstName, item.lastName)
            when (item.position) {
                'E' -> binding.userPosition.text = context.getString(R.string.employee)
                'A' -> binding.userPosition.text = context.getString(R.string.administrator)
                'D' -> binding.userPosition.text = context.getString(R.string.delivery_user)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentAssignUsersRecyclerViewAdapterBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, parent.context)
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