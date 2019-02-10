package com.gmail.jaaska.jaakko.calendarcountdown.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper

class CountdownAppWidgetRemoteViewsFactory(private val context: Context)
    : RemoteViewsService.RemoteViewsFactory {

    private lateinit var countdowns: ArrayList<CountdownSettings>

    override fun onCreate() {
        DatabaseHelper(context, DatabaseHelper.DB_NAME, DatabaseHelper.DB_VERSION).apply {
            openDb()
            countdowns = ArrayList(loadSettingsForWidget())
            countdowns.sort()
            closeDb()
        }
    }

    override fun getLoadingView(): RemoteViews {
        // TODO Consider adding a separate view for "Loading..." state
        return RemoteViews(context.packageName, R.layout.widget_layout_empty)
    }

    override fun getItemId(position: Int): Long = countdowns[position].dbId.toLong()

    override fun onDataSetChanged()  = Unit

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val countdown = countdowns[position]
        val views = RemoteViews(context.packageName, R.layout.widget_listitem_countdown)
        views.setTextViewText(R.id.days, "${countdown.daysToEndDate}")
        views.setTextViewText(R.id.title, countdown.label)
        views.setTextViewText(R.id.daysUntilLabel, context.getString(R.string.countdowns_list_days_until))

        return views
    }

    override fun getCount(): Int = countdowns.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() = Unit
}