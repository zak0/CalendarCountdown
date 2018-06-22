package com.gmail.jaaska.jaakko.calendarcountdown.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings;
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper;
import com.gmail.jaaska.jaakko.calendarcountdown.R;

/**
 * Implementation of App Widget functionality.
 */
public class CountdownAppWidget extends AppWidgetProvider {

    private static final String TAG = "CountdownAppWidget";

    private CountdownSettings settings;

    public CountdownAppWidget() {

    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.d(TAG, "updateAppWidget() - called!!!");

        //settings = new CountdownSettings().loadFromSharedPrefs(context.getSharedPreferences(MainActivity.PREFS_NAME, 0));

        // Load settings from DB
        DatabaseHelper db = new DatabaseHelper(context, DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
        db.openDb();

        /*
        if(db.loadSettings().size() > 0) {
            settings = db.loadSettings().get(0);
        }*/

        settings = db.loadSettingsForWidget();
        db.closeDb();

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_app_widget);

        if(settings != null)
            views.setTextViewText(R.id.appwidget_text, Integer.toString(settings.getDaysToEndDate()));


        // TODO: Open the app when tapping the widget?


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

