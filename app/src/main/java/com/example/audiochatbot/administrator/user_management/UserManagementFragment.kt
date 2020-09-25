package com.example.audiochatbot.administrator.user_management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.administrator.user_management.recycler_view_adapters.UserListener
import com.example.audiochatbot.administrator.user_management.recycler_view_adapters.UserManagementFragmentRecyclerViewAdapter
import com.example.audiochatbot.administrator.user_management.view_models.UserManagementViewModel
import com.example.audiochatbot.administrator.user_management.view_models.UserManagementViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentUserManagementBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class UserManagementFragment : Fragment() {

    private lateinit var testViewModel: UserManagementViewModel
    private var line = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentUserManagementBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_user_management, container, false)

        val application = requireNotNull(this.activity).application
        val args = UserManagementFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.adminId
        val businessId: Int = args.businessId

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            UserManagementViewModelFactory(businessId, userDataSource)

        testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(UserManagementViewModel::class.java)

        testViewModel.navigateToUserDetails.observe(viewLifecycleOwner, { userId ->
            userId?.let {
                this.findNavController().navigate(
                    UserManagementFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(
                        userId)
                )
                testViewModel.onUserNavigated()
            }
        })

        val adapter =
            UserManagementFragmentRecyclerViewAdapter(
                UserListener { userId ->
                    testViewModel.onUserClicked(userId)
                })
        binding.userList.adapter = adapter

        binding.createNewUser.setOnClickListener {
            this.findNavController().navigate(UserManagementFragmentDirections.actionTestFragmentToCreateUserFragment(adminId))
        }

        binding.showList.setOnClickListener {
            line = binding.storeId.text.toString().trim()
            testViewModel.retrieveList(line)
        }

        testViewModel.users.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        testViewModel.retrieveList(line)
    }
}