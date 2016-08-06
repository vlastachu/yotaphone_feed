package me.vlastachu.feedwidget.model;


import android.widget.TableLayout;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.queries.Query;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import static me.vlastachu.feedwidget.model.db.FeedTable.*;

@Root(strict=false, name="channel")
@StorIOSQLiteType(table = TABLE)
public class Feed {
    @StorIOSQLiteColumn(name = COLUMN_ID, key = true) Long id;
    @StorIOSQLiteColumn(name = COLUMN_TITLE)         @Element String   title;
    @StorIOSQLiteColumn(name = COLUMN_LINK)          @Element String   link;
    @StorIOSQLiteColumn(name = COLUMN_NAME)                   String   name;
    @StorIOSQLiteColumn(name = COLUMN_FAVICON_PATH)           String   faviconPath;
    @StorIOSQLiteColumn(name = COLUMN_COLOR)                  long     color;
    @StorIOSQLiteColumn(name = COLUMN_IS_ACTIVE)              boolean  isActive;
    @Element(required=false) Image image;
    @ElementList(inline=true) List<FeedItem> items;

    public static Query queryAllActive = Query.builder()
            .table(TABLE)
            .where(COLUMN_IS_ACTIVE + " = ?")
            .whereArgs(1)
            .build();

    public static Query queryAll = Query.builder()
            .table(TABLE)
            .build();

    public Feed() {
        isActive = true;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public String getFaviconPath() {
        return faviconPath;
    }

    public long getColor() {
        return color;
    }

    public List<FeedItem> getItems() {
        return items;
    }
}
