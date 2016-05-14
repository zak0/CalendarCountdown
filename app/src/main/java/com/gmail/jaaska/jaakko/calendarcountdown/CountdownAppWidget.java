package com.gmail.jaaska.jaakko.calendarcountdown;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class CountdownAppWidget extends AppWidgetProvider {

    private static final String TAG = "CountdownAppWidget";

    private CountdownSettings settings;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.d(TAG, "updateAppWidget() - called!!!");

        settings = new CountdownSettings().loadFromSharedPrefs(context.getSharedPreferences(MainActivity.PREFS_NAME, 0));

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_app_widget);
        views.setTextViewText(R.id.appwidget_text, Integer.toString(settings.getDaysToEndDate()));


        // Set Intent and listener for manual update of the widget
        /*
        Intent intent = new Intent(context, CountdownAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        ComponentName name = new ComponentName(context, CountdownAppWidget.class);
        int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        */

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d(TAG, "onUpdate() - called!!!");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

