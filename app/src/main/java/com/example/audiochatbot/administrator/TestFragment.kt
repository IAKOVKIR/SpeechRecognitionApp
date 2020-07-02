package com.example.audiochatbot.administrator

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
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.FragmentTestBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class TestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentTestBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_test, container, false)

        val application = requireNotNull(this.activity).application
        //val args = TestF.fromBundle(requireArguments())
        //val myID: Int = args.myId //myID

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory = UserManagementViewModelFactory(userDataSource, application)

        val testViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(UserManagementViewModel::class.java)

        testViewModel.navigateToSleepDataQuality.observe(viewLifecycleOwner, Observer { userId ->
            userId?.let {
                this.findNavController().navigate(
                    TestFragmentDirections
                        .actionSleepTrackerFragmentToSleepDetailFragment(userId))
                testViewModel.onSleepDataQualityNavigated()
            }
        })

        val adapter = TestFragmentRecyclerViewAdapter(UserListener { userId ->
            testViewModel.onUserClicked(userId)
        })
        binding.userList.adapter = adapter

        binding.createNewUser.setOnClickListener {
            this.findNavController().navigate(TestFragmentDirections.actionTestFragmentToCreateUserFragment())
        }

        testViewModel.users.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }

}