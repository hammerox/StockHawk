package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.sam_chordas.android.stockhawk.R;

public class DetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Title
        String symbol = getIntent().getStringExtra(MyStocksActivity.KEY_STOCK_SYMBOL);
        TextView titleView = (TextView) findViewById(R.id.details_title);
        titleView.setText(symbol);

        // Graph
        CandleStickChart chart = (CandleStickChart) findViewById(R.id.chart);
    }
}
