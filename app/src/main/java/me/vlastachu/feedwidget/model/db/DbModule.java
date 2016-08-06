package me.vlastachu.feedwidget.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;

import me.vlastachu.feedwidget.model.Feed;
import me.vlastachu.feedwidget.model.FeedItem;
import me.vlastachu.feedwidget.model.FeedItemSQLiteTypeMapping;
import me.vlastachu.feedwidget.model.FeedSQLiteTypeMapping;

public class DbModule {
    private static StorIOSQLite storIOSQLite;
    private static SQLiteOpenHelper openHelper;

    @NonNull
    public static StorIOSQLite provideStorIOSQLite(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        if (storIOSQLite == null)
            storIOSQLite = DefaultStorIOSQLite.builder()
                    .sqliteOpenHelper(sqLiteOpenHelper)
                    .addTypeMapping(Feed.class, new FeedSQLiteTypeMapping())
                    .addTypeMapping(FeedItem.class, new FeedItemSQLiteTypeMapping())
                    .build();
        return storIOSQLite;
    }

    @NonNull
    public static SQLiteOpenHelper provideSQLiteOpenHelper(@NonNull Context context) {
        if (openHelper == null)
            openHelper = new DbOpenHelper(context);
        return openHelper;
    }
}
