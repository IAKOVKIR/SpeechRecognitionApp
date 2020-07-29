package com.example.audiochatbot.administrator.store_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.store_management.view_models.AssignedUsersViewModel
import com.example.audiochatbot.administrator.store_management.view_models.AssignedUsersViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentAssignedUsersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class AssignedUsersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAssignedUsersBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_assigned_users, container, false)

        val application = requireNotNull(this.activity).application
        val args = AssignedUsersFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.adminId
        val storeId: Int = args.storeId

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            AssignedUsersViewModelFactory(storeId, userDataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(AssignedUsersViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = testViewModel

        binding.lifecycleOwner = this

        testViewModel.navigateToUserDetails.observe(viewLifecycleOwner, Observer { userId ->
            userId?.let {
                this.findNavController().navigate(AssignedUsersFragmentDirections.actionAssignedUsersToUserDetail(userId))
                testViewModel.onUserNavigated()
                testViewModel.onMessageCleared()
            }
        })

        val adapter =
            AssignedUsersFragmentRecyclerViewAdapter(
                UserListener { userId ->
                    testViewModel.onUserClicked(userId)
                },
                RemoveUserListener {userId ->
                    testViewModel.deleteRecord(userId)
                })
        binding.userList.adapter = adapter

        testViewModel.users.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })

        binding.assignUsers.setOnClickListener {
            this.findNavController().navigate(AssignedUsersFragmentDirections.actionAssignedUsersToAssignUsers(adminId, storeId, args.businessId))
        }

        return binding.root
    }

}