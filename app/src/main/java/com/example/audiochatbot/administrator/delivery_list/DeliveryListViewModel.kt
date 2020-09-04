package com.example.audiochatbot.administrator.delivery_list

import androidx.lifecycle.ViewModel
import com.example.audiochatbot.database.UserDao

class DeliveryListViewModel(val businessId: Int,val database: UserDao) : ViewModel() {

    val deliveries = database.getAllDeliveries(businessId)
}