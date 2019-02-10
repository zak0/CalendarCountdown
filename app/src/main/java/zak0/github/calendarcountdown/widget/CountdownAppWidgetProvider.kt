package zak0.github.calendarcountdown.widget

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

import zak0.github.calendarcountdown.data.CountdownSettings
import zak0.github.calendarcountdown.storage.DatabaseHelper
import zak0.github.calendarcountdown.R
import zak0.github.calendarcountdown.ui.MainActivity
import zak0.github.calendarcountdown.ui.SetupActivity

/**
 * The widget.
 */
class CountdownAppWidgetProvider : AppWidgetProvider() {

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

        if (settings.isNotEmpty()) {
            displayCountdowns(context, appWidgetManager, remoteViews, appWidgetId)
        } else {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIndent = PendingIntent.getActivity(context, 0, intent, 0)
            remoteViews.setOnClickPendingIntent(R.id.container, pendingIndent)
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        Log.d(TAG, "onUpdate() - called")
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView)
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

        // Add "template" intent for onClick handlers for list items (each of the countdowns).
        val clickIntent = Intent(context, SetupActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setPendingIntentTemplate(R.id.listView, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        private val TAG = CountdownAppWidgetProvider::class.java.simpleName

        fun sendRefreshBroadcast(context: Context) {
            val intent = Intent(context, CountdownAppWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val name = ComponentName(context, CountdownAppWidgetProvider::class.java)
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}

