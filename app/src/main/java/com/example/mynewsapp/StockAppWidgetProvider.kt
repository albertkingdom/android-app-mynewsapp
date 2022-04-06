package com.example.mynewsapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class StockAppWidgetProvider: AppWidgetProvider() {
    private val TAG = "ExampleAppWidgetProvider"
    val ACTION_CLICK_LIST_VIEW = "CLICK_LIST_VIEW"
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate")
        // Perform this loop procedure for each widget that belongs to this
        // provider.
        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity.
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                /* context = */ context,
                /* requestCode = */  0,
                /* intent = */ Intent(context, MainActivity::class.java),
                /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val serviceIntent = Intent(context, WidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // system can distinguish different widget instance
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)))
            //onclick list view
//            val clickIntent = Intent(context, ExampleAppWidgetProvider::class.java)
//            clickIntent.setAction(ACTION_CLICK_LIST_VIEW)
//            val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0)

            // Get the layout for the widget and attach an on-click listener
            // to the button.
            val views: RemoteViews = RemoteViews(
                context?.packageName,
                R.layout.widget
            ).apply {
                setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                setRemoteAdapter(R.id.widget_list_view, serviceIntent) //set adapter for list view
                setEmptyView(R.id.widget_list_view, R.id.example_widget_empty_view)
                setTextViewText(
                    R.id.widget_update_time,
                    context?.getString(R.string.widget_update_time, updateTime())
                )
                //onclick list view
//                setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntent)
            }

            // Tell the AppWidgetManager to perform an update on the current
            // widget.
            appWidgetManager.updateAppWidget(appWidgetId, views)
            // Update listview data by invoke onDataSetChanged()
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)


        if (intent?.action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val name = ComponentName(context, StockAppWidgetProvider::class.java)
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(name))
        }
    }

    private fun updateTime(): String {
        val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a")
        val zoneId = ZoneId.of("Asia/Taipei")
        val localTime = LocalDateTime.now(zoneId)
        val timeString = localTime.format(timeFormatter)
        //println("localTime..$timeString")
        return timeString
    }
}