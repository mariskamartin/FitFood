package com.gmail.mariska.fitfood.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gmail.mariska.fitfood.MainActivity;
import com.gmail.mariska.fitfood.R;
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


public class FitFoodSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String INTENT_TYPE_SERVICE_SYNC = "INTENT_TYPE_SERVICE_SYNC";
    private static final String LOG_TAG = FitFoodSyncAdapter.class.getSimpleName();
    private static final int SYNC_INTERVAL = 60 * (60*24); // 60 seconds (1 minute) * (60*24) = 24 hours
    private static final String LAST_SERVER_UPDATE = "LAST_SERVER_UPDATE";
    private static final long DEFAULT_START_DATE = 1420070400000L; //01.01.2015
    private static final int FOOD_NOTIFICATION_ID = 3004;

    public FitFoodSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Perform synchronisation for Food data
     * @param account -
     * @param extras -
     * @param authority -
     * @param provider -
     * @param syncResult -
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting data synchronisation.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastUpdate = prefs.getLong(LAST_SERVER_UPDATE, DEFAULT_START_DATE);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String foodsJsonStr;

        try {
            final String FITFOOD_BASE_URL = "http://fitfood-mariskamartin.rhcloud.com/api/v1/foods/query?";
            Uri builtUri = Uri.parse(FITFOOD_BASE_URL).buildUpon().appendQueryParameter("since", String.valueOf(lastUpdate)).build();

            URL url = new URL(builtUri.toString());
            Log.d(LOG_TAG, builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            foodsJsonStr = buffer.toString();
            List<Food> newFoods = Utility.fromJson(foodsJsonStr, new TypeReference<List<Food>>() {
            });
            Log.d(LOG_TAG, "count of new foods = " + newFoods.size());

            FitFoodDbHelper dbHelper = new FitFoodDbHelper(getContext());
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
                    long rowId = db.replaceOrThrow(FitFoodContract.FoodEntry.TABLE_NAME, null, foodValues);
                    Log.d(LOG_TAG, "inserted rodID = " + rowId);
                    Log.d(LOG_TAG, "added or updated " + food.getName());
//                    Uri insertFoodUri = getContext().getContentResolver().insert(FitFoodContract.FoodEntry.buildFoodAllUri(), foodValues);
//                    Log.d(LOG_TAG, "inserted food uri = " + insertFoodUri); //this gets complicated when it it sync from another process
                }
            } finally {
                db.close(); //always close
            }

            //save information about last success update from server
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(LAST_SERVER_UPDATE, new Date().getTime());
            editor.commit();

            notifyAboutNewFoods(newFoods, getContext());


            Log.d(LOG_TAG, "try to refresh FoodLoader in FoodListFragment");
            //send intent to perform activity refresh
            Intent intent = new Intent(INTENT_TYPE_SERVICE_SYNC);
            // some data not important
            intent.putExtra("SERVICE_FOODS_UPDATE", true);
            getContext().sendBroadcast(intent); // finally broadcast

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error in inserting data.", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return;
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
    }

    /**
     * Internally performs notifications about new arrived foods
     * @param newFoods list of new foods
     */
    private static void notifyAboutNewFoods(List<Food> newFoods, Context context) {
        Log.v(LOG_TAG, "notifyAboutNewFoods start");

        //checking the last update and notify if it' the first of the day
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
//        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
//                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

//        if ( displayNotifications ) {
//
//            String lastNotificationKey = context.getString(R.string.pref_last_notification);
//            long lastSync = prefs.getLong(lastNotificationKey, 0);
//
//            Log.d(LOG_TAG, "should has notificate user = " + (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS));
//            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.

        if (newFoods.size() > 0){
            Resources resources = context.getResources();
            Food firstFood = newFoods.get(0);
            Bitmap largeIcon;
            if (firstFood.getImg() != null) {
                largeIcon = BitmapFactory.decodeByteArray(firstFood.getImg(), 0, firstFood.getImg().length);
            } else {
                largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.noimage);
            }
            int height = (int) resources.getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) resources.getDimension(android.R.dimen.notification_large_icon_width);
            //fix large icon
            largeIcon = Bitmap.createScaledBitmap(largeIcon, width, height, true);

            StringBuilder sb = new StringBuilder();
            for (Food food : newFoods) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(food.getName());
            }
            Log.d(LOG_TAG, "creates notification");
            // NotificationCompatBuilder is a very convenient way to build backward-compatible
            // notifications.  Just throw in some data.
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setAutoCancel(true)
                            .setColor(resources.getColor(R.color.fitfood_light_green))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(largeIcon)
                            .setContentTitle(String.format(resources.getString(R.string.notification_downloaded), newFoods.size()))
                            .setContentText(sb.toString());

            // Make something interesting happen when the user clicks on the notification.
            // In this case, opening the app is sufficient.
            Intent resultIntent = new Intent(context, MainActivity.class);
//
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(FOOD_NOTIFICATION_ID, mBuilder.build());
            Log.d(LOG_TAG, "notification end");
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        Log.v(LOG_TAG, "configurePeriodicSync - authority = " + authority);
        ContentResolver.addPeriodicSync(account, authority, Bundle.EMPTY, syncInterval);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.v(LOG_TAG,"syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
//        Log.d(LOG_TAG, "getSyncAccount - start");
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
            Log.d(LOG_TAG, "getSyncAccount - need to create new Account");
            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.d(LOG_TAG, "onAccountCreated - success");
        /*
         * Since we've created an account
         */
        FitFoodSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        Log.d(LOG_TAG, "onAccountCreated - performing immediate synchronisation");
        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}