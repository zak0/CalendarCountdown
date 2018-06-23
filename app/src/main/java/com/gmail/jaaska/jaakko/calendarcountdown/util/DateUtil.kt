package com.gmail.jaaska.jaakko.calendarcountdown.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    fun formatDate(dateInMillis: Long): String = SimpleDateFormat("d.M.yyyy", Locale.US).format(Date(dateInMillis))
}