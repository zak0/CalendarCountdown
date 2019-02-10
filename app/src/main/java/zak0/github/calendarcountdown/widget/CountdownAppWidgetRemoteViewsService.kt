package zak0.github.calendarcountdown.widget

import android.content.Intent
import android.widget.RemoteViewsService

class CountdownAppWidgetRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =  CountdownAppWidgetRemoteViewsFactory(applicationContext)
}