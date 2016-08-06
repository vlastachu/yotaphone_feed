package me.vlastachu.feedwidget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.util.List;

import me.vlastachu.feedwidget.model.Feed;
import me.vlastachu.feedwidget.model.Model;

public class FeedsAdapter extends RecyclerView.Adapter {
    private static final int EMPTY_TYPE = 0;
    private static final int FEED_TYPE = 1;
    public static final String TAG = FeedsAdapter.class.getSimpleName();
    private final Handler handler;

    private List<Feed> feeds;
    private Context ctx;
    private boolean wasChanged = false;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView feedAddress;
        public View container;
        public ImageView favicon;
        public TextView title;
        public ViewHolder(View v) {
            super(v);
            container = v;
            feedAddress = (TextView) v.findViewById(R.id.feed_address);
            title = (TextView) v.findViewById(R.id.feed_title);
            favicon = (ImageView) v.findViewById(R.id.favicon);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View container;
        public EditText feedAddress;
        public ProgressBar progressBar;
        public EmptyViewHolder(View v) {
            super(v);
            container = v;
            feedAddress = (EditText) v.findViewById(R.id.feed_address_edit_text);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        }
    }


    public FeedsAdapter(List<Feed> feeds, Context context) {
        this.feeds = feeds;
        ctx = context;
        handler = new Handler();
    }

    @Override
    public int getItemViewType(int position) {
        if (feeds.get(position) == null)
            return EMPTY_TYPE;
        else return FEED_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: viewType == FEED_TYPE " + (viewType == FEED_TYPE));
        if (viewType == FEED_TYPE) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_feed_item, parent, false);
            return new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.settings_empty_item, parent, false);
            return new EmptyViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == EMPTY_TYPE) {
            onBindEmptyViewHolder((EmptyViewHolder) holder, position);
        } else {
            onBindFeedViewHolder((ViewHolder)holder, position);
        }
    }

    private void onBindFeedViewHolder(ViewHolder holder, int position) {
        Feed feed = feeds.get(position);
        holder.container.setBackgroundColor((int) feed.getColor());
        holder.feedAddress.setText(feed.getName());
        holder.title.setText(feed.getTitle());
        Log.d(TAG, "onBindFeedViewHolder: " + feed.getTitle());
        Bitmap bitmap = BitmapFactory.decodeFile(feed.getFaviconPath());
        holder.favicon.setImageBitmap(bitmap);
    }

    private void onBindEmptyViewHolder(EmptyViewHolder holder, int position) {
        holder.feedAddress.setText("");
        holder.progressBar.setAlpha(0);
        holder.feedAddress.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        boolean prevalidation = Model.getInstance().validateAndInsertFeedSource(
                                ctx, v.getText().toString(),
                                feed -> {
                                    wasChanged = true;
                                    feeds.set(position, feed);
                                    handler.post(() -> notifyItemChanged(position));
                                },
                                exception -> {
                                    String message;
                                    if (exception instanceof MalformedURLException) {
                                        message = ctx.getString(R.string.settings_error_format);
                                    } else {
                                        message = ctx.getString(R.string.settings_error_network);
                                    }
                                    handler.post(() -> {
                                        holder.feedAddress.setError(message);
                                        holder.progressBar.animate().alpha(0).setDuration(200).start();
                                    });
                                });
                        if (!prevalidation) {
                            String message = ctx.getString(R.string.settings_error_format);
                            holder.feedAddress.setError(message);
                        } else {
                            holder.progressBar.animate().alpha(1).setDuration(200).start();
                        }
                    }
                    return false;
                }
        );
    }

    public boolean wasChanged() {
        return wasChanged;
    }

    public void removeAtPosition(int position) {
        wasChanged = true;
        Model.getInstance().getStorIOSQLite(ctx)
                .delete()
                .object(feeds.get(position))
                .prepare().executeAsBlocking();
        feeds.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public void addEmptyField() {
        feeds.add(null);
        notifyItemInserted(feeds.size() - 1);
    }
}
