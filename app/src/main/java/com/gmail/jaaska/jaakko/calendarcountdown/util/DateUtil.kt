package com.gmail.jaaska.jaakko.calendarcountdown.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    private const val FORMAT = "d.M.yyyy"

    fun formatDate(dateInMillis: Long): String = formatDate(Date(dateInMillis))
    fun formatDate(date: Date): String = SimpleDateFormat(FORMAT, Locale.US).format(date)
}