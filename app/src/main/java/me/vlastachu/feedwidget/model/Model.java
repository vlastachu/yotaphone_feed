package me.vlastachu.feedwidget.model;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;

import org.apache.commons.io.IOUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import me.vlastachu.feedwidget.model.db.DbModule;
import me.vlastachu.feedwidget.model.db.FeedItemTable;

public class Model {

    public interface Callback<T> {
        void callback(T t);
    }

    public static final String TAG = Model.class.getSimpleName();
    private static final int tryingsReloadForFeed = 3;

    private static Model instance;
    private SQLiteOpenHelper openHelper;
    private StorIOSQLite storIOSQLite;
    private static Random random = new Random();
    private Model() {
    }

    @NonNull
    public static Model getInstance(){
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public StorIOSQLite getStorIOSQLite(Context context) {
        if (storIOSQLite == null) {
            initByContext(context);
        }
        return storIOSQLite;
    }

    public Feed parseXml(InputStream is) throws Exception {
        Serializer serializer = new Persister();
        RSSChannel channel = serializer.read(RSSChannel.class, is);
        return channel.channel;
    }

    public void refresh(Runnable onComplete, Context context) {
        new Thread(() -> {
            initByContext(context);
            List<Feed> feeds = storIOSQLite
                    .get()
                    .listOfObjects(Feed.class)
                    .withQuery(Feed.queryAllActive)
                    .prepare()
                    .executeAsBlocking();
            for (Feed feed: feeds) {
                for (int i = 0; i < tryingsReloadForFeed; ++i) {
                    try {
                        URL url = new URL(feed.getLink());
                        Log.d(TAG, "refresh: " + feed.getLink());
                        InputStream is = url.openStream();
                        List<FeedItem> parsedFeedItems = parseXml(is).getItems();
                        for (FeedItem feedItem: parsedFeedItems) {
                            feedItem.setParentFeed(feed);
                        }

                        storIOSQLite.delete()
                                .byQuery(DeleteQuery.builder()
                                        .table(FeedItemTable.TABLE)
                                        .where(FeedItemTable.COLUMN_FEED_ID + " = ?")
                                        .whereArgs(feed.getId())
                                        .build())
                                .prepare().executeAsBlocking();
                        storIOSQLite.put()
                                .objects(parsedFeedItems)
                                .prepare().executeAsBlocking();
                        break;
                    } catch (IOException e) {
                        Log.d(TAG, i + " trying to reload feed;");
                        Log.d(TAG, "refresh exception: " + e + "; message: " + e.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "refresh exception: " + e + "; message: " + e.getMessage(), e);
                        break;
                    }
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }).start();
    }

    public boolean validateAndInsertFeedSource(Context ctx, String source, Callback<Feed> onSuccess,
                                               Callback<Exception> onFail) {
        URI uri;
        try {
            uri = new URI(source);
            // validate url
            if(uri.getScheme() == null || uri.getScheme().isEmpty())
                new URL("http://" + source);
            else new URL(source);
        } catch (URISyntaxException|MalformedURLException e) {
            Log.d(TAG, "validateAndInsertFeedSource: invalid uri " + source);
            return false;
        }
        new Thread(() -> {
            try {
                InputStream is = null;
                URI uri_ = uri;
                if (TextUtils.isEmpty(uri_.getScheme())) {
                    for (String scheme : new String[]{"http", "https"}) {
                        Log.d(TAG, "run: scheme changed to " + scheme);
                        uri_ = new URI(scheme + "://" + source);
                        URL url = new URL(scheme + "://" + source);
                        try {
                            is = url.openStream();
                        } catch (IOException e) {
                            continue;
                        }
                        break;
                    }
                    if(is == null) throw new IOException();
                } else {
                    Log.d(TAG, "validateAndInsertFeedSource: " + source);
                    is = new URL(source).openStream();
                }
                Feed feed = parseXml(is);
                String domain = uri_.getHost();
                feed.name = domain.startsWith("www.") ? domain.substring(4) : domain;
                feed.title = feed.title.replace("\n", " ").replace("\r", " ");
                feed.link = uri_.toString();
                Log.d(TAG, "validateAndInsertFeedSource: uri = " + feed.link);

                if (feed.image != null && !TextUtils.isEmpty(feed.image.url)) {
                    feed.faviconPath = saveFaviconToFile(feed.image.url, domain + "_favicon.png", ctx);
                } else {
                    String iconPath = uri_.getScheme() + "://" + domain + "/favicon.ico";
                    feed.faviconPath = saveFaviconToFile(iconPath, domain + "_favicon.ico", ctx);
                }
                feed.color = extractColor(feed.faviconPath);

                storIOSQLite.put().object(feed).prepare().executeAsBlocking();
                onSuccess.callback(feed);
            } catch (MalformedURLException e) {
                Log.e(TAG, "validateAndInsertFeedSource: invalid state. " + e);
            } catch (IOException e) {
                Log.d(TAG, "validateAndInsertFeedSource: failed connection. " + e.getMessage());
                onFail.callback(e);
            } catch (Exception e) {
//                Log.d(TAG, "validateAndInsertFeedSource: failed parsing. " + e);
//                onFail.callback(e);
                Log.e(TAG, "validateAndInsertFeedSource: ", e);
            }
        }).start();
        return true;
    }

    private String saveFaviconToFile(String faviconSource, String outputPath, Context ctx) {
        try {
            File file = new File(ctx.getFilesDir(), outputPath);
            if (file.exists())
                return file.getAbsolutePath();
            InputStream is = new URL(faviconSource).openStream();
            OutputStream os = ctx.openFileOutput(outputPath, Context.MODE_PRIVATE);
            IOUtils.copy(is, os);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.d(TAG, "saveFaviconToFile: " + e);
        }
        return null;
    }

    private int extractColor(String imagePath) {
        int defaultColor = getRandomColor();
        File image = new File(imagePath);
        if(!image.exists())
            return defaultColor;
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        Palette p = Palette.from(bitmap).maximumColorCount(4).generate();
        if (p.getDarkVibrantColor(p.getVibrantColor(-1)) != -1)
            return p.getDarkVibrantColor(p.getVibrantColor(defaultColor));
        if(p.getSwatches().get(0).getHsl()[2] < 0.8)
            return p.getSwatches().get(0).getRgb();
        return defaultColor;
    }

    private int getRandomColor() {
        final float hue = random.nextFloat()*360;
        final float saturation = (random.nextInt(2000) + 5000) / 10000f;
        final float luminance = 0.8f;
        return Color.HSVToColor(new float[]{hue, saturation, luminance});
    }

    public void getFeeds(Callback<List<Feed>> callback) {
        new Thread(() -> {
            List<Feed> feeds = storIOSQLite.get()
                    .listOfObjects(Feed.class)
                    .withQuery(Feed.queryAll)
                    .prepare().executeAsBlocking();
            feeds = new ArrayList<Feed>(feeds);
            if (feeds.isEmpty()) feeds.add(null);
            callback.callback(feeds);
        }).start();
    }

    public void initByContext(Context context) {
        openHelper = DbModule.provideSQLiteOpenHelper(context);
        storIOSQLite = DbModule.provideStorIOSQLite(openHelper);
    }
}
