package com.example.audiochatbot.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.databinding.UserDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
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

        binding.goBack.setOnClickListener {
            this.findNavController().popBackStack()
        }

        return binding.root
    }
}