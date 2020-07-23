package com.example.audiochatbot.administrator.user_management

import android.os.Bundle
import android.view.Gravity
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
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModel
import com.example.audiochatbot.administrator.user_management.view_models.CreateUserViewModelFactory
import com.example.audiochatbot.database.UniDatabase
import com.example.audiochatbot.database.User
import com.example.audiochatbot.databinding.FragmentCreateUserBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


/**
 * A simple [Fragment] subclass.
 */
class CreateUserFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentCreateUserBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_user, container, false)
        val application = requireNotNull(this.activity).application
        val args = CreateUserFragmentArgs.fromBundle(requireArguments())
        val adminId: Int = args.adminId

        val userDataSource = UniDatabase.getInstance(application, CoroutineScope(Dispatchers.Main)).userDao

        val viewModelFactory =
            CreateUserViewModelFactory(userDataSource)

        val viewModel =
            ViewModelProvider(
                this, viewModelFactory).get(CreateUserViewModel::class.java)

        val adapter = ArrayAdapter(this.requireContext(),
            android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Positions))
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                viewModel.setPos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
        binding.submit.setOnClickListener {
            val user = User()
            user.firstName = binding.firstName.text.trim().toString()
            user.lastName = binding.lastName.text.trim().toString()
            user.email = binding.email.text.trim().toString()
            user.phoneNumber = binding.phoneNumber.text.trim().toString()
            user.password = binding.password.text.trim().toString()
            viewModel.submitUser(user, adminId)
        }

        viewModel.isUploaded.observe(viewLifecycleOwner, Observer {result ->
            if (result)
                this.findNavController().popBackStack()
            else {
                val toast = Toast.makeText(context, "Something went wrong :(", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()
            }
        })

        return binding.root
    }
}