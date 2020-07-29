package com.example.audiochatbot.administrator.store_management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiochatbot.database.User
import com.example.audiochatbot.database.UserDao
import com.example.audiochatbot.databinding.FragmentAssignUsersRecyclerViewAdapterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssignUsersRecyclerViewAdapter(private val clickListener: AssignedUserListener,
                                               private val addUserListener: AddUserListener, private val database: UserDao,
                                     private val storeId: Int) : ListAdapter<User,
        AssignUsersRecyclerViewAdapter.ViewHolder>(AssignUsersDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, addUserListener, item, database, storeId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: FragmentAssignUsersRecyclerViewAdapterBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(clickListener: AssignedUserListener, addUserListener: AddUserListener, item: User, database: UserDao, storeId: Int) {
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

                return ViewHolder(binding)
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

class AssignedUserListener(val clickListener: (userId: Int) -> Unit) {
    fun onClick(user: User) = clickListener(user.userId)
}

class AddUserListener(val clickListener: (userId: Int) -> Unit) {
    fun onAddUser(user: User) = clickListener(user.userId)
}