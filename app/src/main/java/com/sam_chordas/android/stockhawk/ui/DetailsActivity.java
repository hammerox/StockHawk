package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.DailyValues;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DetailsActivity extends Activity {

    public final static int DAYS_COUNT = 30;
    public final static int DAYS_TO_SHOW = 15;

    private String symbol;
    private CandleStickChart mChart;
    private XAxis xAxis;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Calendar
        calendar = Calendar.getInstance();

        // Title
        symbol = getIntent().getStringExtra(MyStocksActivity.KEY_STOCK_SYMBOL).toUpperCase();
        TextView titleView = (TextView) findViewById(R.id.details_title);
        titleView.setText(symbol);

        // Graph
        setCandleChart();

        mChart = (CandleStickChart) findViewById(R.id.chart);
        mChart.setMaxVisibleValueCount(DAYS_TO_SHOW);
        mChart.setDrawGridBackground(false);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("USD");
        mChart.setDescriptionColor(Color.WHITE);

        xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setLabelCount(7, false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextColor(Color.WHITE);

        // Get data
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonArray = preferences.getString(symbol, null);
        if (jsonArray != null) {
            Log.d("getPreferences", "Array check: OK");
            JSONArray resultsArray = null;
            try {
                resultsArray = new JSONArray(jsonArray);
                String endDate = resultsArray.getJSONObject(0).getString("Date");
                Log.d("getPreferences", "endDate: " + endDate);
                if (Utils.isYesterday(calendar, endDate)) {
                    Log.d("getPreferences", "Date check: OK - Date is correct");
                    setData(setDataList(resultsArray));
                } else {
                    if (Utils.dateIsLastFriday(calendar, endDate)) {
                        Log.d("getPreferences", "Date check: OK - Date is last friday");
                        setData(setDataList(resultsArray));
                    } else {
                        Log.d("getPreferences", "Date check: FALSE - Is not a valid date");
                        new FetchHistoricValues().execute(symbol);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("getPreferences", "Array check: FALSE - Fetching new data");
            new FetchHistoricValues().execute(symbol);
        }
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
        set1.setValueTextColor(Color.WHITE);
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
        xAxis.setValueFormatter(new DateAxisFormatter(dailyValues));
    }


    public void setCandleChart() {
        mChart = (CandleStickChart) findViewById(R.id.chart);
        mChart.setMaxVisibleValueCount(DAYS_TO_SHOW);
        mChart.setDrawGridBackground(false);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("USD");
        mChart.setDescriptionColor(Color.WHITE);

        xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);

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
    }


    public List<DailyValues> setDataList(JSONArray jsonArray){
        List<DailyValues> list = new ArrayList<>();
        JSONObject jsonObject = null;

        try {
            int size = jsonArray.length();
            for (int i = size - 1; i >= 0; i--) {
                jsonObject = jsonArray.getJSONObject(i);

                DailyValues values = new DailyValues();
                values.setDate(jsonObject.getString("Date"));
                values.setOpen(jsonObject.getString("Open"));
                values.setHigh(jsonObject.getString("High"));
                values.setLow(jsonObject.getString("Low"));
                values.setClose(jsonObject.getString("Close"));
                values.setVolume(jsonObject.getLong("Volume"));
                values.setAdjClose(jsonObject.getString("Adj_Close"));

                list.add(values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }


    public class FetchHistoricValues extends AsyncTask<String,Void,String> {

        private String startDate;
        private String endDate;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder urlStringBuilder = new StringBuilder();
            endDate = Utils.getTodayDate();
            startDate = Utils.getStartDate(calendar, endDate, DAYS_COUNT);

            Log.d("FetchHistoricValues", "start: " + startDate + " - end: " + endDate);

            try{
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(
                        URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ", "UTF-8"));
                urlStringBuilder.append("\"" + params[0] + "\"");
                urlStringBuilder.append(
                        URLEncoder.encode(" and startDate = \"" + startDate
                                + "\" and endDate =\"" + endDate + "\"", "UTF-8"));
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
                Log.d("FetchHistoricValues", urlStringBuilder.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String jsonString = null;
            if (urlStringBuilder != null) {
                try {
                    jsonString = Utils.fetchData(urlStringBuilder.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /*TODO - Warn user when connection to server fails*/

            return jsonString;
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            super.onPostExecute(jsonResult);

            if (jsonResult != null) {
                Log.d("FetchHistoricValues", jsonResult);

                List<DailyValues> resultList = new ArrayList<>();
                JSONObject jsonObject = null;
                JSONArray resultsArray = null;
                try {
                    // Get jsonArray.
                    jsonObject = new JSONObject(jsonResult);
                    if (jsonObject != null && jsonObject.length() != 0) {
                        jsonObject = jsonObject.getJSONObject("query");
                        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                        // If jsonArray is valid ...
                        if (resultsArray != null && resultsArray.length() != 0) {

                            // save array to SharedPreferences ...
                            SharedPreferences preferences = PreferenceManager
                                    .getDefaultSharedPreferences(DetailsActivity.this);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(symbol, resultsArray.toString());
                            editor.apply();

                            // and add values to data list.
                            resultList = setDataList(resultsArray);
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
    }


    public class DateAxisFormatter implements AxisValueFormatter {

        List<DailyValues> dailyValues;

        public DateAxisFormatter(List<DailyValues> dailyValues) {
            this.dailyValues = dailyValues;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int i = (int) value;
            String dateString = dailyValues.get(i).getDate();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = inputFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat axisFormat = new SimpleDateFormat("MM/dd");

            if (date != null) {
                return axisFormat.format(date);
            } else {
                return String.valueOf(i);
            }
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }
    }
}
