package com.gmail.mariska.fitfood;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gmail.mariska.fitfood.data.FitFoodContract;
import com.gmail.mariska.fitfood.data.FitFoodContract.FoodEntry;

/**
 * Fragment for Main Activity with list of food
 */
public class FoodListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = FoodListFragment.class.getSimpleName();
    /**
     * Food Loader ID
     */
    public static final int FOOD_LOADER_ID = 0;

    private int mPosition = ListView.INVALID_POSITION;

    /**
     * Definition of columns for loading data
     */
    private static final String[] FOOD_COLUMNS = {
            FoodEntry.TABLE_NAME + "." + FoodEntry._ID,
            FoodEntry.COLUMN_NAME,
            FoodEntry.COLUMN_TEXT,
            FoodEntry.COLUMN_AUTHOR,
            FoodEntry.COLUMN_CREATED,
            FoodEntry.COLUMN_UPDATED,
            FoodEntry.COLUMN_RATING,
            FoodEntry.COLUMN_IMG
    };
    static final int COL_FOOD_ID = 0;
    static final int COL_FOOD_NAME = 1;
    static final int COL_FOOD_TEXT = 2;
    static final int COL_FOOD_AUTHOR = 3;
    static final int COL_FOOD_CREATED = 4;
    static final int COL_FOOD_UPDATED = 5;
    static final int COL_FOOD_RATING = 6;
    static final int COL_FOOD_IMG = 7;

    private static final String SELECTED_ROW_KEY = "SELECTED_ROW";

    private FoodListAdapter mFoodListAdapter;
    private ListView mListView;
    private String mDefaultFoodListSortOrder = FoodEntry.COLUMN_UPDATED + " DESC";


    public FoodListFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FOOD_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mFoodListAdapter = new FoodListAdapter(getActivity(), null, 0);
        mListView = (ListView) rootView.findViewById(R.id.listview_food);
        mListView.setAdapter(mFoodListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Uri detailUri = FoodEntry.buildConcreteFood(String.valueOf(cursor.getInt(COL_FOOD_ID)));
                    ((Callback) getActivity()).onListItemSelected(detailUri);
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_ROW_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_ROW_KEY);
        }

        listAllFood();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Get the SearchView and set the searchable configuration
            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            // Assumes current activity is the searchable activity
            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String s) {
                    Log.v(LOG_TAG, "Submitted search: " + s);
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String searchStr) {
                    Log.v(LOG_TAG, "Searching: " + searchStr);
                    listSearchedFood(searchStr.trim()); //because of automatic filling
                    return true; //true - widget stay unfolded
                }
            });

            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    Log.v(LOG_TAG, "Searching collapsed.");
                    listAllFood();
                    return true;  // Return true to collapse action view
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // Do something when expanded
                    return true;  // Return true to expand action view
                }
            });
        }
    }



    /**
     * Search for foods and shows them in list.
     * @param searchStr
     */
    public void listSearchedFood(String searchStr) {
        Log.v(LOG_TAG, "creates new cursor for: " + searchStr);
        Cursor searchCursor = getActivity().getContentResolver().query(FoodEntry.buildFoodSearch(searchStr), null, null, null, mDefaultFoodListSortOrder );
        mFoodListAdapter.swapCursor(searchCursor);
    }

    /**
     * Shows all foods.
     */
    public void listAllFood() {
        Cursor foodCursor = getActivity().getContentResolver().query(FitFoodContract.FoodEntry.CONTENT_URI, null, null, null, mDefaultFoodListSortOrder );
        mFoodListAdapter.swapCursor(foodCursor);
    }

    /**
     * Restarts loader. It starts to refresh data.
     */
    public void restartFoodLoader(){
        getLoaderManager().restartLoader(FOOD_LOADER_ID, null, this);
    }

    //methods for cursor loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri weatherForLocationUri = FoodEntry.buildFoodAllUri();
        return new CursorLoader(getActivity(), weatherForLocationUri, FOOD_COLUMNS, null, null, mDefaultFoodListSortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFoodListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFoodListAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //when we are going out.. so save position
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_ROW_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public interface Callback {
        void onListItemSelected(Uri foodDetailUri);
    }
}
