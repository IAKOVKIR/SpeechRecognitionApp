package com.example.audiochatbot.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.UserDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [UserDetailFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [UserDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class UserDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: UserDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.user_detail, container, false)

        val application = requireNotNull(this.activity).application
        val arguments = UserDetailFragmentArgs.fromBundle(requireArguments())

        // Create an instance of the ViewModel Factory.
        val dataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao
        val viewModelFactory = UserDetailViewModelFactory(arguments.userKey, dataSource)

        // Get a reference to the ViewModel associated with this fragment.
        val userDetailViewModel =
            ViewModelProvider(
            this, viewModelFactory).get(UserDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.userDetailViewModel = userDetailViewModel

        binding.lifecycleOwner = this

        // Add an Observer to the state variable for Navigating when a Quality icon is tapped.
        /*userDetailViewModel.navigateToSleepTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    UserDetailFragmentDirections.actionSleepDetailFragmentToSleepTrackerFragment())
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                userDetailViewModel.doneNavigating()
            }
        })*/

        return binding.root
    }
}