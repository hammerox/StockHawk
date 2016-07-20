package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

	private static String LOG_TAG = Utils.class.getSimpleName();

	public static boolean showPercent = true;

	public static ArrayList quoteJsonToContentVals(Context context, String JSON){
		ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
		JSONObject jsonObject = null;
		JSONArray resultsArray = null;
		try{
			jsonObject = new JSONObject(JSON);
			if (jsonObject != null && jsonObject.length() != 0){
				jsonObject = jsonObject.getJSONObject("query");
				int count = Integer.parseInt(jsonObject.getString("count"));
				if (count == 1){
					jsonObject = jsonObject.getJSONObject("results")
							.getJSONObject("quote");
					addToList(context, jsonObject, batchOperations);
				} else{
					resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

					if (resultsArray != null && resultsArray.length() != 0){
						for (int i = 0; i < resultsArray.length(); i++){
							jsonObject = resultsArray.getJSONObject(i);
							addToList(context, jsonObject, batchOperations);
						}
					}
				}
			}
		} catch (JSONException e){
			Log.e(LOG_TAG, "String to JSON failed: " + e);
		}
		return batchOperations;
	}

	public static String truncateBidPrice(String bidPrice){
		/*Todo - DEBUG - java.lang.NumberFormatException: Invalid float: "null"*/
		bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
		return bidPrice;
	}

	public static String truncateChange(String change, boolean isPercentChange){
		String weight = change.substring(0,1);
		String ampersand = "";
		if (isPercentChange){
			ampersand = change.substring(change.length() - 1, change.length());
			change = change.substring(0, change.length() - 1);
		}
		change = change.substring(1, change.length());
		double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
		change = String.format("%.2f", round);
		StringBuffer changeBuffer = new StringBuffer(change);
		changeBuffer.insert(0, weight);
		changeBuffer.append(ampersand);
		change = changeBuffer.toString();
		return change;
	}

	public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
		ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
				QuoteProvider.Quotes.CONTENT_URI);
		Log.d("json", jsonObject.toString());
		try {
			String change = jsonObject.getString("Change");

			if (change.equals("null")) {
				return null;
			}

			builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
			builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
			builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
					jsonObject.getString("ChangeinPercent"), true));
			builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
			builder.withValue(QuoteColumns.ISCURRENT, 1);
			if (change.charAt(0) == '-'){
				builder.withValue(QuoteColumns.ISUP, 0);
			}else{
				builder.withValue(QuoteColumns.ISUP, 1);
			}

		} catch (JSONException e){
			e.printStackTrace();
		}
		return builder.build();
	}

	public static void addToList(final Context context, JSONObject jsonObject, ArrayList<ContentProviderOperation> batchOperations) {
		ContentProviderOperation operation = buildBatchOperation(jsonObject);
		if (operation != null) {
			batchOperations.add(operation);
		} else {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context.getApplicationContext(),
							R.string.toast_symbol_not_found,
							Toast.LENGTH_LONG)
							.show();
				}
			});
			Log.e(LOG_TAG, "Cannot find new symbol");
		}
	}


    public static String fetchData(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }


    public static String getStartDate(String endDate, int daysCount) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DATE, -daysCount);
        return sdf.format(calendar.getTime());
    }
}
