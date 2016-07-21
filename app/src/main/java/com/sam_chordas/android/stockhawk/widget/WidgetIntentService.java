package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by Mauricio on 20-Jul-16.
 */
public class WidgetIntentService extends IntentService {

    private final static String[] QUOTE_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CREATED,
            QuoteColumns.ISCURRENT,
            QuoteColumns.ISUP
    };

    private final static int INDEX_ID = 0;
    private final static int INDEX_SYMBOL = 1;
    private final static int INDEX_BIDPRICE = 2;
    private final static int INDEX_CHANGE = 3;
    private final static int INDEX_PERCENT_CHANGE = 4;
    private final static int INDEX_CREATED = 5;
    private final static int INDEX_ISCURRENT = 6;
    private final static int INDEX_ISUP = 7;

    
    public WidgetIntentService() {
        super("PercentWidgetIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = widgetManager
                .getAppWidgetIds(new ComponentName(this, WidgetProvider.class));

        Cursor data = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        QUOTE_COLUMNS,
                        null,
                        null,
                        QuoteColumns.CREATED + " ASC");

        if (data == null) return;
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        String symbol = data.getString(INDEX_SYMBOL).toUpperCase();
        String bidPrice = data.getString(INDEX_BIDPRICE);
        String valueReal = data.getString(INDEX_CHANGE);
        String valuePercent = data.getString(INDEX_PERCENT_CHANGE);
        boolean isUp = data.getInt(INDEX_ISUP) == 1;

        data.close();

        // Perform loop on every widget available
        for (int appWidgetId : appWidgetIds) {

            // Get layout according to widget size
            int widgetWidth = getWidgetWidth(widgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_size_medium);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_size_large);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget_medium;
            } else {
                layoutId = R.layout.widget_small;
            }

            RemoteViews views = new RemoteViews(this.getPackageName(), layoutId);

            // Add data
            views.setTextViewText(R.id.widget_symbol, symbol);
            views.setTextViewText(R.id.widget_value_real, valueReal);
            views.setTextViewText(R.id.widget_value_percent, valuePercent);
            views.setTextViewText(R.id.widget_bidprice, bidPrice);

            // Format color
            if (isUp) {
                views.setInt(R.id.widget_value_percent, "setBackgroundResource",
                        R.drawable.percent_change_pill_green);
            } else {
                views.setInt(R.id.widget_value_percent, "setBackgroundResource",
                        R.drawable.percent_change_pill_red);
            }

            // Create intent to launch activity on click
            Intent launchIntent = new Intent(this, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Perform update on the current app widget
            widgetManager.updateAppWidget(appWidgetId, views);
        }

    }


    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_size_medium);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_size_medium);
    }
}
