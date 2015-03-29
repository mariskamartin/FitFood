package com.gmail.mariska.fitfood;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.mariska.fitfood.data.FitFoodContract.FoodEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int FOOD_DETAIL_LOADER_ID = 1;
    /**
     * Definition of columns for loading data into detail
     */
    private static final String[] FOOD_DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            FoodEntry.TABLE_NAME + "." + FoodEntry._ID,
            FoodEntry.COLUMN_NAME,
            FoodEntry.COLUMN_TEXT,
            FoodEntry.COLUMN_AUTHOR,
            FoodEntry.COLUMN_CREATED,
            FoodEntry.COLUMN_UPDATED,
            FoodEntry.COLUMN_RATING,
            FoodEntry.COLUMN_IMG
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_FOOD_ID = 0;
    static final int COL_FOOD_NAME = 1;
    static final int COL_FOOD_TEXT = 2;
    static final int COL_FOOD_AUTHOR = 3;
    static final int COL_FOOD_CREATED = 4;
    static final int COL_FOOD_UPDATED = 5;
    static final int COL_FOOD_RATING = 6;
    static final int COL_FOOD_IMG = 7;

    public static final String DETAIL_URI = "URI";

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private final static String FOOD_SHARE_HASHTAG = "#FitFoodApp";

    private String mFoodTxt;
    private ShareActionProvider mShareActionProvider;
    private ImageView mIconView;
    private TextView mDateView;
    private TextView mFriendlyDateView;
    private TextView mForecastView;
    private TextView mHighView;
    private TextView mLowView;
    private TextView mWindView;
    private TextView mHumidityView;
    private TextView mPressureView;
    private String mLocation;
    private Uri mUri;

    public DetailFragment() {
        setHasOptionsMenu(true); //for onCreateOptionMenu
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.food_detail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.e(LOG_TAG, "Share action provider is null?");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
           mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View view = inflater.inflate(R.layout.fragment_food_detail, container, false);
        mIconView = (ImageView) view.findViewById(R.id.detail_icon);
        mDateView = (TextView) view.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) view.findViewById(R.id.detail_day_textview);
        mForecastView = (TextView) view.findViewById(R.id.detail_forecast_textview);
        mHighView = (TextView) view.findViewById(R.id.detail_high_textview);
        mLowView = (TextView) view.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) view.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) view.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) view.findViewById(R.id.detail_pressure_textview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FOOD_DETAIL_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri == null) {
            return null;
        }
        return new CursorLoader(getActivity(), mUri, FOOD_DETAIL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            Log.v(LOG_TAG, "onLoadFinished - no data");
            return;
        }
        Log.v(LOG_TAG, "onLoadFinished - loading");
//        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITIONID)));

//        long dateInMillis = data.getLong(COL_WEATHER_DATE);
//        String dateString = Utility.formatDate(dateInMillis);
//        String friendlyDateString = Utility.getFriendlyDayString(getActivity(), dateInMillis);
//        mFriendlyDateView.setText(friendlyDateString);
//        mDateView.setText(dateString);
//
//        String weatherDescription = data.getString(COL_WEATHER_DESC);
//        mForecastView.setText(weatherDescription);
//        mIconView.setContentDescription(weatherDescription);
//
//        boolean isMetric = Utility.isMetric(getActivity());
//        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP));
//        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP));
//        mHighView.setText(high);
//        mLowView.setText(low);
//
//        mHumidityView.setText("Vlhkost vzduchu: " + data.getFloat(COL_WEATHER_HUMID) + " %");
//
//        String formattedWind = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WEATHER_WINDSPEES), data.getFloat(COL_WEATHER_DEGREES));
//        mWindView.setText(formattedWind);
//        mPressureView.setText("Tlak: " + data.getFloat(COL_WEATHER_PRESSURE) + " hPa");
        String foodName = cursor.getString(FoodListFragment.COL_FOOD_NAME);
        String foodText = cursor.getString(FoodListFragment.COL_FOOD_TEXT);
        String author = cursor.getString(FoodListFragment.COL_FOOD_AUTHOR);
        String rating = cursor.getInt(FoodListFragment.COL_FOOD_RATING) + "/10";
        mFoodTxt = String.format("Food: %s\nAuthor:%s (rating %s)\n%s\n", foodName, author, rating, foodText);

        mForecastView.setText(mFoodTxt);


        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //ONLY IN CASE WHEN SETTINGS CHANGE
//        Intent intent = getActivity().getIntent();
//        if (intent != null && intent.hasExtra(DetailActivity.DATE_KEY) &&
//                mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
//            getLoaderManager().restartLoader(FOOD_DETAIL_LOADER_ID, null, this);
//        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); //for 21 API is newer FALG
        //FIXME - remake it to Rich Content
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFoodTxt + FOOD_SHARE_HASHTAG);
        return shareIntent;
    }

}