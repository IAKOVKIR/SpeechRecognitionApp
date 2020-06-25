package com.example.audiochatbot.employee

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.audiochatbot.LoginActivity
import com.example.audiochatbot.R
import com.example.audiochatbot.databinding.FragmentEmployeeHomeBinding

/**
 * A simple [Fragment] subclass.
 */
class EmployeeHomeFragment : Fragment() {
    //private lateinit var n: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentEmployeeHomeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_employee_home, container, false)

        //n = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        binding.logOutButton.setOnClickListener {
            logOut()
        }

        return binding.root
    }

    /**
     * @function [logOut] removes all data from SharedPreferences and starts Login activity
     * removing all the activities and fragments that were not destroyed before
     */
    private fun logOut() {
        /*val editor: SharedPreferences.Editor = n.edit()
        editor.clear()
        editor.apply()*/

        val loginIntent = Intent(requireActivity(), LoginActivity::class.java)
        // set the new task and clear flags
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
        requireActivity().finish()
    }
}