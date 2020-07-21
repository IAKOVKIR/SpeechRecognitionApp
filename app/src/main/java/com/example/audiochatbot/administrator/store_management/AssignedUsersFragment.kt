package com.example.audiochatbot.administrator.store_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.store_management.view_models.AssignedUsersViewModel
import com.example.audiochatbot.administrator.store_management.view_models.AssignedUsersViewModelFactory
import com.example.audiochatbot.administrator.user_management.UserManagementFragmentDirections
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
            AssignedUsersViewModelFactory(adminId, storeId, userDataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(AssignedUsersViewModel::class.java)

        testViewModel.navigateToUserDetails.observe(viewLifecycleOwner, Observer { userId ->
            userId?.let {
                this.findNavController().navigate(
                    UserManagementFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(
                        userId)
                )
                testViewModel.onUserNavigated()
            }
        })

        val adapter =
            AssignedUsersFragmentRecyclerViewAdapter(
                UserListener { userId ->
                    testViewModel.onUserClicked(userId)
                })
        binding.userList.adapter = adapter

        testViewModel.users.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.assignUser.setOnClickListener {
            val line = binding.userIdText.text.toString().trim()
            testViewModel.assignUser(line)
        }

        return binding.root
    }

}