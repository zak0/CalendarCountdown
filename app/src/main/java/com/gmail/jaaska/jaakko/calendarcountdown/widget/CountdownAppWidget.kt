package com.gmail.jaaska.jaakko.calendarcountdown.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews

import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper
import com.gmail.jaaska.jaakko.calendarcountdown.R

/**
 * The widget.
 */
class CountdownAppWidget : AppWidgetProvider() {

    private var settings: CountdownSettings? = null

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                appWidgetId: Int) {

        Log.d(TAG, "updateAppWidget() - called")

        // Load settings from DB
        DatabaseHelper(context, DatabaseHelper.DB_NAME, DatabaseHelper.DB_VERSION).apply {
            openDb()
            settings = loadSettingsForWidget()
            closeDb()
        }

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.countdown_app_widget)

        settings?.also {
            views.setTextViewText(R.id.appwidget_text, Integer.toString(it.daysToEndDate))
        }

        // TODO: Open the app when tapping the widget?

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        Log.d(TAG, "onUpdate() - called")
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        private val TAG = CountdownAppWidget::class.java.simpleName
    }
}

