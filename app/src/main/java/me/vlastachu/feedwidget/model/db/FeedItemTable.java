package me.vlastachu.feedwidget.model.db;

import android.support.annotation.NonNull;

public class FeedItemTable {
    public static final String TABLE                = "feed_items";
    public static final String COLUMN_ID            = "_id";
    public static final String COLUMN_TITLE         = "title";
    public static final String COLUMN_LINK          = "link";
    public static final String COLUMN_DESCRIPTION   = "description";
    public static final String COLUMN_PUB_DATE      = "pub_date";
    public static final String COLUMN_COLOR         = "color";
    public static final String COLUMN_FEED_NAME     = "feed_name";
    public static final String COLUMN_FEED_ICON     = "feed_icon";
    public static final String COLUMN_FEED_ID       = "_feed_id";

    private FeedItemTable() {}

    @NonNull
    public static String getCreateTableQuery() {
        return "CREATE TABLE " + TABLE + "("
                + COLUMN_ID            + " INTEGER NOT NULL PRIMARY KEY, "
                + COLUMN_TITLE         + " TEXT NOT NULL, "
                + COLUMN_LINK          + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION   + " TEXT NOT NULL, "
                + COLUMN_FEED_NAME     + " TEXT NOT NULL, "
                + COLUMN_FEED_ICON     + " TEXT NOT NULL, "
                + COLUMN_PUB_DATE      + " INTEGER NOT NULL, "
                + COLUMN_COLOR         + " INTEGER NOT NULL, "
                + COLUMN_FEED_ID       + " INTEGER NOT NULL"
                + ");";
    }
}
