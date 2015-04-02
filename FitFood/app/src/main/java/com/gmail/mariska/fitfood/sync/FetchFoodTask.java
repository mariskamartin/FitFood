package com.gmail.mariska.fitfood.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.mariska.fitfood.Utility;
import com.gmail.mariska.fitfood.data.FitFoodContract;
import com.gmail.mariska.fitfood.data.FitFoodDbHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;


public class FetchFoodTask extends AsyncTask<String, Void, Void> {

    public static final String LAST_SERVER_UPDATE = "LAST_SERVER_UPDATE";
    private static final long DEFAULT_START_DATE = 1420070400000L; //01.01.2015
    private final String LOG_TAG = FetchFoodTask.class.getSimpleName();
    private final Context mContext;
    private static final ObjectMapper mapper = new ObjectMapper();

    public FetchFoodTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        long lastUpdate = prefs.getLong(LAST_SERVER_UPDATE, DEFAULT_START_DATE);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String foodsJsonStr = null;

        try {
            final String FITFOOD_BASE_URL = "http://fitfood-mariskamartin.rhcloud.com/api/v1/foods";
            Uri builtUri = Uri.parse(FITFOOD_BASE_URL).buildUpon().appendQueryParameter("since", String.valueOf(lastUpdate)).build();

            URL url = new URL(builtUri.toString());
            Log.d(LOG_TAG, builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            foodsJsonStr = buffer.toString();
            Log.d(LOG_TAG, foodsJsonStr);
            List<Food> newFoods = Utility.fromJson(foodsJsonStr, new TypeReference<List<Food>>() {});

            FitFoodDbHelper dbHelper = new FitFoodDbHelper(mContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                for (Food food : newFoods) {
                    ContentValues foodValues = new ContentValues();
                    foodValues.put(FitFoodContract.FoodEntry._ID, food.getId());
                    foodValues.put(FitFoodContract.FoodEntry.COLUMN_AUTHOR, food.getAuthor());
                    foodValues.put(FitFoodContract.FoodEntry.COLUMN_CREATED, food.getCreated().getTime());
                    foodValues.put(FitFoodContract.FoodEntry.COLUMN_UPDATED, food.getUpdated().getTime());
                    foodValues.put(FitFoodContract.FoodEntry.COLUMN_NAME, food.getName());
                    foodValues.put(FitFoodContract.FoodEntry.COLUMN_TEXT, food.getText());
                    foodValues.put(FitFoodContract.FoodEntry.COLUMN_RATING, food.getRating());
                    foodValues.put(FitFoodContract.FoodEntry.COLUMN_IMG, food.getImg());
                    long rowId = db.insertOrThrow(FitFoodContract.FoodEntry.TABLE_NAME, null, foodValues);
                    Log.d(LOG_TAG, "inserted rodID = " + rowId);
                }
            } finally {
                db.close(); //always close
            }

            //save information about last success update from server
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(LAST_SERVER_UPDATE, new Date().getTime());
            editor.commit();

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error in inserting data.", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }
}