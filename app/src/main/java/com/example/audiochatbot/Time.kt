package com.example.audiochatbot

import java.text.SimpleDateFormat
import java.util.*

class Time {

    fun getDate(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(c.time).trim()
    }

    fun getTime(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(c.time).trim()
    }

    override fun toString(): String {
        return "${getDate()} ${getTime()}"
    }
}