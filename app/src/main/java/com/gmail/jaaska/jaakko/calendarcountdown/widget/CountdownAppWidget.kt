package com.gmail.jaaska.jaakko.calendarcountdown.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper
import com.gmail.jaaska.jaakko.calendarcountdown.R

/**
 * The widget.
 */
class CountdownAppWidget : AppWidgetProvider() {

    private var settings: List<CountdownSettings> = ArrayList()

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                appWidgetId: Int) {

        Log.d(TAG, "updateAppWidget() - called")

        // Load settings from DB
        DatabaseHelper(context, DatabaseHelper.DB_NAME, DatabaseHelper.DB_VERSION).apply {
            openDb()
            settings = loadSettingsForWidget()
            closeDb()
        }

        // If there are no countdowns to display on the widget, show the layout instructing
        // to open the app to set them up...
        //
        // Otherwise display the countdowns.
        val remoteViews = RemoteViews(context.packageName,
                if (settings.isNotEmpty()) R.layout.widget_layout_populated
                else R.layout.widget_layout_empty)

        // Setup the layout of the widget
        displayCountdowns(context, appWidgetManager, remoteViews, appWidgetId)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
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

    /**
     * Starts the service that will populate the widget.
     */
    private fun displayCountdowns(context: Context,
                                  appWidgetManager: AppWidgetManager,
                                  views: RemoteViews,
                                  appWidgetId: Int) {
        val intent = Intent(context, CountdownAppWidgetRemoteViewsService::class.java)
        views.setRemoteAdapter(R.id.listView, intent)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        private val TAG = CountdownAppWidget::class.java.simpleName
    }
}

