package me.vlastachu.feedwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.DrawableMarginSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.pushtorefresh.storio.sqlite.queries.Query;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import me.vlastachu.feedwidget.model.FeedItem;
import me.vlastachu.feedwidget.model.Model;

import static me.vlastachu.feedwidget.model.db.FeedItemTable.COLUMN_PUB_DATE;
import static me.vlastachu.feedwidget.model.db.FeedItemTable.TABLE;

public class FeedWidget extends AppWidgetProvider {

    private static class FeedWidgetData {
        public final List<URL> urls = new ArrayList<>();
    }

    /** Tag for LogCat. */
    private static final String TAG             = FeedWidget.class.getSimpleName();
    private static final String DOWN_CLICKED    = "DOWN_CLICKED";
    private static final String UP_CLICKED      = "UP_CLICKED";
    private static final String ITEM_CLICKED    = "ITEM_CLICKED";
    private static final String PREVIEW_CLICKED = "PREVIEW_CLICKED";
    private static final String SAVE_CLICKED    = "SAVE_CLICKED";
    private static final String ROTATE_CLICKED  = "ROTATE_CLICKED";
    private static final String CLOSE_CLICKED   = "CLOSE_CLICKED";
    private static final Map<Integer, FeedWidgetData> data = new HashMap<>();

    private static final int[] titles = new int[]{R.id.title1, R.id.title2, R.id.title3, R.id.title4};
    private static final int[] contents = new int[]{R.id.content1, R.id.content2, R.id.content3, R.id.content4};
    private static final int[] content_menues = new int[]{R.id.content_menu1, R.id.content_menu1,
            R.id.content_menu1, R.id.content_menu1};
    private static final int[] preview_buttons = new int[]{R.id.preview_button1, R.id.preview_button1,
            R.id.preview_button1, R.id.preview_button1};
    private static final int[] save_buttons = new int[]{R.id.save_button1, R.id.save_button1,
            R.id.save_button1, R.id.save_button1};
    private static final int[] rotate_buttons = new int[]{R.id.rotate_button1, R.id.rotate_button1,
            R.id.rotate_button1, R.id.rotate_button1};
    private static final int[] close_buttons = new int[]{R.id.close_button1, R.id.close_button1,
            R.id.close_button1, R.id.close_button1};
    private static int offset = 0;
    private static List<FeedItem> items = new ArrayList<>();


    static void updateWidget(final Context context, final AppWidgetManager widgetManager, final int id) {
        Log.d(TAG, "updateWidgets: Widget id = " + id);

        // Get widget options
        //widgetOptions = widgetManager.getAppWidgetOptions(id);

        final String resourceUrl = SettingsActivity.loadTitlePref(context, id);
        items = FeedItem.getItemsByOffset(context, offset, 5);
        updateView(context, -1);
    }

    private static CharacterData getTagsContent(Element element, String tagName) {
        return (CharacterData) element.getElementsByTagName(tagName).item(0).getFirstChild();
    }

    private static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, FeedWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (UP_CLICKED.equals(action) || DOWN_CLICKED.equals(action)) {
            if (UP_CLICKED.equals(intent.getAction())) {
                if (offset > 0) {
                    offset -= titles.length;
                } else {
                    Model.getInstance().refresh(() -> updateView(context, -1), context);
                    return;
                }
            } else {
                if (items.size() < 5) {
                    Model.getInstance().refresh(() -> updateView(context, -1), context);
                    offset = 0;
                    return;
                }
                offset += titles.length;
            }
            items = FeedItem.getItemsByOffset(context, offset, 5);
            updateView(context, -1);
        }
        if(action.startsWith(ITEM_CLICKED)) {
            int number = Integer.parseInt(action.substring(ITEM_CLICKED.length()));
            updateView(context, number);
        }
        if(action.startsWith(PREVIEW_CLICKED) || action.startsWith(SAVE_CLICKED) ||
                action.startsWith(ROTATE_CLICKED) || action.startsWith(CLOSE_CLICKED)) {
            updateView(context, -1);
        }
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
    private static void updateView(Context context, int showMenu) {
        final int[] ids = getWidgetIds(context);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        if (offset == 0)
            views.setImageViewResource(R.id.button_up, R.mipmap.ic_refresh_white_36dp);
        else
            views.setImageViewResource(R.id.button_up, R.mipmap.ic_keyboard_arrow_up_white_36dp);

        if (items.size() < 5)
            views.setImageViewResource(R.id.button_down, R.mipmap.ic_refresh_white_36dp);
        else
            views.setImageViewResource(R.id.button_down, R.mipmap.ic_keyboard_arrow_down_white_36dp);


        views.setOnClickPendingIntent(R.id.button_up, getPendingSelfIntent(context, UP_CLICKED));
        views.setOnClickPendingIntent(R.id.button_down, getPendingSelfIntent(context, DOWN_CLICKED));

        for (int i = 0; i < contents.length; i++) {
            views.setOnClickPendingIntent(contents[i], getPendingSelfIntent(context,
                    ITEM_CLICKED + i));
        }

        for (int i = 0; i < titles.length; i++) {
            if (i + offset < items.size()) {
                FeedItem item = items.get(i + offset);
                views.setTextViewText(titles[i], item.getTitle());
                views.setTextViewText(contents[i], Html.fromHtml(removeImagesAndNewlines(item.getDescription())));
                views.setViewVisibility(content_menues[i], View.GONE);
            }
        }

        if (showMenu != -1) {
            int n = showMenu;
            views.setViewVisibility(content_menues[n], View.VISIBLE);
            views.setOnClickPendingIntent(preview_buttons[n], getPendingSelfIntent(context,
                    PREVIEW_CLICKED + n));
            views.setOnClickPendingIntent(save_buttons[n], getPendingSelfIntent(context,
                    SAVE_CLICKED + n));
            views.setOnClickPendingIntent(rotate_buttons[n], getPendingSelfIntent(context,
                    ROTATE_CLICKED + n));
            views.setOnClickPendingIntent(close_buttons[n], getPendingSelfIntent(context,
                    CLOSE_CLICKED + n));
        }

        // Instruct the widget manager to update the widget
        for (int id: ids) {
            Log.d(TAG, "updateWidgets: Widget id = " + id);
            widgetManager.updateAppWidget(id, views);
        }
    }

    private static String removeImagesAndNewlines(String text) {
        String filtered = text.replaceAll("(<br\\s?/>|<img\\s+src=\"[^\"]*\"[^(/>)]*/>)","");
        return filtered;
    }

    /**
     * Returns all ids of this widget instances.
     * */
    private static int[] getWidgetIds(Context context) {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,
                FeedWidget.class));
    }
}