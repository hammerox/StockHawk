package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
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
        String valuePercent = data.getString(INDEX_PERCENT_CHANGE);
        boolean isUp = data.getInt(INDEX_ISUP) == 1;

        data.close();

        // Perform loop on every widget available
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(
                    this.getPackageName(),
                    R.layout.widget_small);

            // Add data
            views.setTextViewText(R.id.widget_symbol, symbol);
            views.setTextViewText(R.id.widget_value_percent, valuePercent);

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
}
