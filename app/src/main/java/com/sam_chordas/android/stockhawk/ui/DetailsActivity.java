package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.DailyValues;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends Activity {

    private CandleStickChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Title
        String symbol = getIntent().getStringExtra(MyStocksActivity.KEY_STOCK_SYMBOL);
        symbol = symbol.toUpperCase();
        TextView titleView = (TextView) findViewById(R.id.details_title);
        titleView.setText(symbol);

        // Graph
        mChart = (CandleStickChart) findViewById(R.id.chart);
        mChart.setMaxVisibleValueCount(20);
        mChart.setDrawGridBackground(false);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("USD");
        mChart.setDescriptionColor(Color.WHITE);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setLabelCount(7, false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setLabelCount(7, false);
        rightAxis.setDrawAxisLine(true);
        rightAxis.setTextColor(Color.WHITE);

        new FetchHistoricValues().execute(symbol);
    }


    public void setData(List<DailyValues> dailyValues) {
        mChart.resetTracking();

        ArrayList<CandleEntry> yVals1 = new ArrayList<>();

        int count = dailyValues.size();

        for (int i = 0; i < count; i++) {
            DailyValues values = dailyValues.get(i);
            yVals1.add(
                    new CandleEntry(
                            i,
                            values.getHigh(),
                            values.getLow(),
                            values.getOpen(),
                            values.getClose()
                    )
            );
        }

        CandleDataSet set1 = new CandleDataSet(yVals1, "Data Set");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setShadowColor(Color.WHITE);
        set1.setShadowWidth(2f);
        set1.setDecreasingColor(Color.RED);
        set1.setDecreasingPaintStyle(Paint.Style.FILL);
        set1.setIncreasingColor(Color.rgb(122, 242, 84));
        set1.setIncreasingPaintStyle(Paint.Style.FILL);
        set1.setNeutralColor(Color.BLUE);

        CandleData data = new CandleData(set1);

        mChart.setData(data);
        mChart.invalidate();
    }


    public class FetchHistoricValues extends AsyncTask<String,Void,String> {


        @Override
        protected String doInBackground(String... params) {
            StringBuilder urlStringBuilder = new StringBuilder();
            try{
                // Base URL for the Yahoo query
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(
                        URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ", "UTF-8"));
                urlStringBuilder.append("\"" + params[0] + "\"");
                urlStringBuilder.append(
                        URLEncoder.encode(" and startDate = \"2015-05-01\" and endDate =\"2015-05-19\"", "UTF-8"));
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
                Log.d("FetchHistoricValues", urlStringBuilder.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String jsonString = null;
            if (urlStringBuilder != null) {
                try {
                    jsonString = fetchData(urlStringBuilder.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return jsonString;
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            super.onPostExecute(jsonResult);
            Log.d("FetchHistoricValues", jsonResult);

            List<DailyValues> resultList = new ArrayList<>();
            JSONObject jsonObject = null;
            JSONArray resultsArray = null;
            try{
                jsonObject = new JSONObject(jsonResult);
                if (jsonObject != null && jsonObject.length() != 0){
                    jsonObject = jsonObject.getJSONObject("query");
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0){
                        int size = resultsArray.length();
                        for (int i = size - 1; i >= 0; i--){
                            jsonObject = resultsArray.getJSONObject(i);

                            DailyValues values = new DailyValues();
                            values.setDate(jsonObject.getString("Date"));
                            values.setOpen(jsonObject.getString("Open"));
                            values.setHigh(jsonObject.getString("High"));
                            values.setLow(jsonObject.getString("Low"));
                            values.setClose(jsonObject.getString("Close"));
                            values.setVolume(jsonObject.getLong("Volume"));
                            values.setAdjClose(jsonObject.getString("Adj_Close"));

                            resultList.add(values);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (resultList.size() > 0) {
                setData(resultList);
            }
        }
    }


    String fetchData(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
