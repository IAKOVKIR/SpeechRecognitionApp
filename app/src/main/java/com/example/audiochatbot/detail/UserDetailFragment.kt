package com.example.audiochatbot.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.audiochatbot.R
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.database.User
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

        val adapter = ArrayAdapter(this.requireContext(),
            android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Positions))
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                userDetailViewModel.setPos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        userDetailViewModel.user.observe(viewLifecycleOwner, Observer {pos ->
            when (pos.position) {
                'E' -> {
                    binding.spinner.setSelection(0)
                    userDetailViewModel.setPos(0)
                }
                'A' -> {
                    binding.spinner.setSelection(1)
                    userDetailViewModel.setPos(1)
                }
                else -> {
                    binding.spinner.setSelection(2)
                    userDetailViewModel.setPos(2)
                }
            }
        })

        binding.submit.setOnClickListener {
            val user = User()
            user.firstName = binding.firstName.text.trim().toString()
            user.lastName = binding.lastName.text.trim().toString()
            user.email = binding.email.text.trim().toString()
            user.phoneNumber = binding.phoneNumber.text.trim().toString()
            user.password = binding.password.text.trim().toString()
            userDetailViewModel.updateUser(user)
        }

        userDetailViewModel.isUploaded.observe(viewLifecycleOwner, Observer {result ->
            if (result)
                this.findNavController().popBackStack()
            else
                Toast.makeText(context, "Something went wrong :(", Toast.LENGTH_SHORT).show()
        })

        //binding.goBack.setOnClickListener {
          //  this.findNavController().popBackStack()
        //}

        return binding.root
    }
}