package me.vlastachu.feedwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.yotadevices.sdk.BackscreenLauncherConstants;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedWidget extends AppWidgetProvider {

    private static class FeedWidgetData {
        public final List<URL> urls = new ArrayList<>();
    }

    /** Tag for LogCat. */
    private static final String LOG_TAG = FeedWidget.class.getSimpleName();
    private static final Map<Integer, FeedWidgetData> data = new HashMap<>();

    static void updateWidget(Context context, AppWidgetManager widgetManager, int id) {
        Log.d(LOG_TAG, "updateWidgets: Widget id = " + id);

        // Get widget options
        //widgetOptions = widgetManager.getAppWidgetOptions(id);

        CharSequence widgetText = SettingsActivity.loadTitlePref(context, id);
        // Construct the RemoteViews object
        RemoteViews views = makeView(context);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        widgetManager.updateAppWidget(id, views);
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Invoke a method from super class, at first
        super.onUpdate(context, appWidgetManager, appWidgetIds);


        // Get the AppWidgetManager instance
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        // Get all ids of this widget instances
        int[] ids = getWidgetIds(context);

        //Bundle widgetOptions;
        for (int id : ids) {
            if(!data.containsKey(id)) {
                data.put(id, new FeedWidgetData());
            }

            updateWidget(context, widgetManager, id);
        }
    }

    /**
     * Make view for widget.
     * */
    private static RemoteViews makeView(Context context) {
        // Create RemoteViews
        return new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    }

    /**
     * Returns all ids of this widget instances.
     * */
    private int[] getWidgetIds(Context context) {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,
                FeedWidget.class));
    }

}