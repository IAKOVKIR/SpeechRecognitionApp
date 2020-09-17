package com.example.audiochatbot.administrator.inventories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.audiochatbot.R

/**
 * A simple [Fragment] subclass.
 */
class InventoryListRecyclerViewAdapter : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_inventory_list_recycler_view_adapter,
            container,
            false
        )
    }
}