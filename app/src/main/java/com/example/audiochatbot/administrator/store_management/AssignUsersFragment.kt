package com.example.audiochatbot.administrator.store_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AddUserListener
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AssignUsersRecyclerViewAdapter
import com.example.audiochatbot.administrator.store_management.recycler_view_adapters.AssignedUserListener
import com.example.audiochatbot.administrator.store_management.view_models.AssignUsersViewModel
import com.example.audiochatbot.administrator.store_management.view_models.AssignUsersViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentAssignUsersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class AssignUsersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAssignUsersBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_assign_users, container, false)

        val application = requireNotNull(this.activity).application
        val args = AssignUsersFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.adminId
        val storeId: Int = args.storeId
        val businessId: Int = args.businessId

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            AssignUsersViewModelFactory(storeId, businessId, userDataSource)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(AssignUsersViewModel::class.java)

        val adapter =
            AssignUsersRecyclerViewAdapter(
                AssignedUserListener { userId ->
                    testViewModel.onUserClicked(userId)
                },
                AddUserListener { userId ->
                    testViewModel.addRecord(userId, adminId)
                })
        binding.userList.adapter = adapter

        testViewModel.users.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        testViewModel.navigateToUserDetails.observe(viewLifecycleOwner, { userId ->
            userId?.let {
                this.findNavController().navigate(AssignUsersFragmentDirections.actionAssignUsersToUserDetail(userId))
                testViewModel.onUserNavigated()
            }
        })

        return binding.root
    }

}