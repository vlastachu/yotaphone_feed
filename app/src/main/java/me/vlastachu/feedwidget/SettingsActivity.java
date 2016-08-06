package me.vlastachu.feedwidget;

import android.animation.Animator;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import java.util.List;

import me.vlastachu.feedwidget.model.Feed;
import me.vlastachu.feedwidget.model.Model;

public class SettingsActivity extends Activity {

    private static final String PREFS_NAME = "layout.NewAppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText mAppWidgetText;
    private RecyclerView recyclerView;
    private FloatingActionButton addButton;
    private FeedsAdapter feedsAdapter;
    private List<Feed> feedsSanpshot;
    private Model model;

    class NothingAnimatorListener implements Animator.AnimatorListener {
        @Override public void onAnimationStart(Animator animation) {}
        @Override public void onAnimationEnd(Animator animation) {}
        @Override public void onAnimationCancel(Animator animation) {}
        @Override public void onAnimationRepeat(Animator animation) {}
    }

    class SlideAnimator extends DefaultItemAnimator {
        @Override
        public boolean animateChange(final RecyclerView.ViewHolder oldHolder, final RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
            Display display = SettingsActivity.this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            oldHolder.itemView.animate().translationX(-width).setDuration(400).setListener(
                    new NothingAnimatorListener() {
                        @Override public void onAnimationStart(Animator animator) {
                            onChangeStarting(oldHolder, true);
                        }
                        @Override public void onAnimationEnd(Animator animator) {
                            onChangeFinished(oldHolder, true);
                        }
                    }).start();
            newHolder.itemView.setTranslationX(width);
            final DefaultItemAnimator self = this;
            newHolder.itemView.animate().translationX(0).setDuration(400).setListener(
                    new NothingAnimatorListener() {
                        @Override public void onAnimationStart(Animator animator) {
                            onChangeStarting(newHolder, false);
                        }
                        @Override public void onAnimationEnd(Animator animator) {
                            onChangeFinished(newHolder, false);
                        }
                    }).start();
            return true;
        }

        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            holder.itemView.setTranslationY(200);
            holder.itemView.setAlpha(0);
            holder.itemView.animate().alpha(1).translationY(0).setDuration(400).setListener(
                    new NothingAnimatorListener() {

                        @Override public void onAnimationStart(Animator animator) {
                            onChangeStarting(holder, false);
                        }
                        @Override public void onAnimationEnd(Animator animator) {
                            onChangeFinished(holder, false);
                        }
                    }).start();
            return true;
        }
    }

    ItemTouchHelper.SimpleCallback removeOnSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if(direction == ItemTouchHelper.LEFT) {
                int position = viewHolder.getAdapterPosition();
                feedsAdapter.removeAtPosition(position);
            }
        }
    };

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = SettingsActivity.this;

            // When the button is clicked, store the string locally
            String widgetText = mAppWidgetText.getText().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            FeedWidget.updateWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return "";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_settings);
        // TODO !!!!!!!!
        mAppWidgetText = new EditText(this);// (EditText) findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        recyclerView = (RecyclerView) findViewById(R.id.feeds_list);
        recyclerView.setItemAnimator(new SlideAnimator());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(removeOnSwipe);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        model = Model.getInstance();
        model.initByContext(this);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        addButton = (FloatingActionButton)findViewById(R.id.add_button);

        mAppWidgetText.setText(loadTitlePref(SettingsActivity.this, mAppWidgetId));

        Context self = this;
        model.getFeeds(feeds ->
            runOnUiThread(() -> {
                feedsSanpshot = feeds;
                feedsAdapter = new FeedsAdapter(feeds, self);
                addButton.setOnClickListener(v -> {
                    feedsAdapter.addEmptyField();
                });
                recyclerView.setAdapter(feedsAdapter);
            }));
    }

    @Override
    protected void onPause() {
        model.getFeeds(feeds -> {
                if (feedsAdapter.wasChanged())
                    model.refresh(null, SettingsActivity.this);
        });
        super.onPause();
    }
}
