package me.vlastachu.feedwidget.model;

import android.content.Context;
import android.util.Log;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.queries.Query;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

import static me.vlastachu.feedwidget.model.db.FeedItemTable.*;

@Root(strict=false, name="item")
@StorIOSQLiteType(table = TABLE)
public class FeedItem {
    private static final String TAG = FeedItem.class.getSimpleName();
    @StorIOSQLiteColumn(name = COLUMN_ID, key = true) Long id;
    @StorIOSQLiteColumn(name = COLUMN_TITLE)       @Element(data=true) String title;
    @StorIOSQLiteColumn(name = COLUMN_DESCRIPTION) @Element(data=true) String description;
    @StorIOSQLiteColumn(name = COLUMN_LINK)        @Element            String link;
    @StorIOSQLiteColumn(name = COLUMN_PUB_DATE)                        int    pubDateInt;
    @StorIOSQLiteColumn(name = COLUMN_FEED_NAME)                       String feedName;
    @StorIOSQLiteColumn(name = COLUMN_FEED_ICON)                       String feedIcon;
    @StorIOSQLiteColumn(name = COLUMN_FEED_ID)                         long   feedId;
    @StorIOSQLiteColumn(name = COLUMN_COLOR)                           long   color;
    @Element String pubDate;

    public static List<FeedItem> getItemsByOffset(Context context, int offset, int count) {
        return Model.getInstance().getStorIOSQLite(context).get()
                .listOfObjects(FeedItem.class)
                .withQuery(Query.builder()
                        .table(TABLE)
                        .orderBy(COLUMN_PUB_DATE)
                        .limit(offset, count)
                        .build())
                .prepare().executeAsBlocking();
    }

    public FeedItem() {
    }

    public void setParentFeed(Feed feed) {
        feedIcon = feed.getFaviconPath();
        feedId   = feed.getId();
        feedName = feed.getName();
        color    = feed.getColor();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }
}
