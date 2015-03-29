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
public class FoodDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int FOOD_DETAIL_LOADER_ID = 1;
    /**
     * Definition of columns for loading data into detail
     */
    private static final String[] FOOD_DETAIL_COLUMNS = {
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

    private final String LOG_TAG = FoodDetailFragment.class.getSimpleName();
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

    public FoodDetailFragment() {
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
           mUri = arguments.getParcelable(FoodDetailFragment.DETAIL_URI);
        }
        View view = inflater.inflate(R.layout.fragment_food_detail, container, false);
        mIconView = (ImageView) view.findViewById(R.id.detail_icon);
        mDateView = (TextView) view.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) view.findViewById(R.id.detail_day_textview);
        mForecastView = (TextView) view.findViewById(R.id.detail_forecast_textview);
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

        //TODO ... detail content

        mIconView.setImageResource(R.drawable.greeksalad);

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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        //FIXME - remake it to Rich Content
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFoodTxt + FOOD_SHARE_HASHTAG);
        return shareIntent;
    }

}
