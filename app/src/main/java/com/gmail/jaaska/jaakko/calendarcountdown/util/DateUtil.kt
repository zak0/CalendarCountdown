package com.gmail.jaaska.jaakko.calendarcountdown.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    private const val FORMAT = "d.M.yyyy"
    private const val DB_FORMAT_STRING = "dd-MM-yyyy"
    private val DB_FORMAT = SimpleDateFormat(DB_FORMAT_STRING, Locale.getDefault())

    fun formatDate(dateInMillis: Long): String = formatDate(Date(dateInMillis))
    fun formatDate(date: Date): String = SimpleDateFormat(FORMAT, Locale.getDefault()).format(date)

    fun formatDatabaseDate(dateInMillis: Long): String = formatDatabaseDate(Date(dateInMillis))
    fun formatDatabaseDate(date: Date): String = DB_FORMAT.format(date)
    fun parseDatabaseDate(dateString: String): Date = DB_FORMAT.parse(dateString)

    fun databaseDateToUiDate(dateString: String): String = formatDate(parseDatabaseDate(dateString))
}