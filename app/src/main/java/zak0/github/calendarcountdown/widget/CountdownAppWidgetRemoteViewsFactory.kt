package zak0.github.calendarcountdown.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import zak0.github.calendarcountdown.R
import zak0.github.calendarcountdown.data.CountdownSettings
import zak0.github.calendarcountdown.storage.DatabaseHelper

class CountdownAppWidgetRemoteViewsFactory(private val context: Context)
    : RemoteViewsService.RemoteViewsFactory {

    private lateinit var countdowns: ArrayList<CountdownSettings>

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        loadCountdownsFromDb()
    }

    override fun getLoadingView(): RemoteViews {
        // TODO Consider adding a separate view for "Loading..." state
        return RemoteViews(context.packageName, R.layout.widget_layout_empty)
    }

    override fun getItemId(position: Int): Long = countdowns[position].dbId.toLong()

    override fun onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged()")
        loadCountdownsFromDb()
    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        Log.d(TAG, "getViewAt($position)")
        val countdown = countdowns[position]
        val views = RemoteViews(context.packageName, R.layout.widget_listitem_countdown)
        views.setTextViewText(R.id.days, "${countdown.daysToEndDate}")
        views.setTextViewText(R.id.title, countdown.label)
        views.setTextViewText(R.id.daysUntilLabel, context.getString(R.string.countdowns_list_days_until))

        val fillInIntent = Intent()
        fillInIntent.putExtra(CountdownSettings.extraName, countdown) // TODO Currently not used, but added because I can...
        views.setOnClickFillInIntent(R.id.container, fillInIntent)

        return views
    }

    override fun getCount(): Int = countdowns.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() = Unit

    private fun loadCountdownsFromDb() {
        DatabaseHelper(context, DatabaseHelper.DB_NAME, DatabaseHelper.DB_VERSION).apply {
            openDb()
            countdowns = ArrayList(loadSettingsForWidget())
            countdowns.sort()
            closeDb()
        }
    }

    companion object {
        private val TAG = CountdownAppWidgetRemoteViewsFactory::class.java.simpleName
    }
}