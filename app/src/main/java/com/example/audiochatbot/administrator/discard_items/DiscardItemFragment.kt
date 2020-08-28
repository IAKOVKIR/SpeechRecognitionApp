package com.example.audiochatbot.administrator.discard_items

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.audiochatbot.R

class DiscardItemFragment : Fragment() {

    /*companion object {
        fun newInstance() = DiscardItemFragment()
    }

    private lateinit var viewModel: DiscardItemViewModel
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.discard_item_fragment, container, false)
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DiscardItemViewModel::class.java)
    }*/

}