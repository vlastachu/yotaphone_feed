package me.vlastachu.feedwidget.model.db;

import android.support.annotation.NonNull;

/**
 * Created by vlastachu on 03.08.16.
 */
public class FeedTable {
    public static final String TABLE                = "feeds";
    public static final String COLUMN_ID            = "_id";
    public static final String COLUMN_IS_ACTIVE     = "is_active";
    public static final String COLUMN_TITLE         = "title";
    public static final String COLUMN_LINK          = "link";
    public static final String COLUMN_NAME          = "name";
    public static final String COLUMN_FAVICON_PATH  = "favicon_path";
    public static final String COLUMN_COLOR         = "color";

    private FeedTable() {}

    @NonNull
    public static String getCreateTableQuery() {
        return "CREATE TABLE " + TABLE + "("
                + COLUMN_ID            + " INTEGER NOT NULL PRIMARY KEY, "
                + COLUMN_IS_ACTIVE     + " INTEGER NOT NULL, "
                + COLUMN_TITLE         + " TEXT NOT NULL, "
                + COLUMN_LINK          + " TEXT NOT NULL, "
                + COLUMN_NAME          + " TEXT NOT NULL, "
                + COLUMN_FAVICON_PATH  + " TEXT, "
                + COLUMN_COLOR         + " INTEGER NOT NULL"
                + ");";
    }
}
