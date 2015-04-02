package com.gmail.mariska.fitfood;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.mariska.fitfood.data.FitFoodContract;
import com.gmail.mariska.fitfood.data.FitFoodDbHelper;
import com.gmail.mariska.fitfood.sync.FetchFoodTask;

import java.util.Date;


/**
 * Main FIt Food activity.
 */
public class MainActivity extends ActionBarActivity implements FoodListFragment.Callback {

    private static final String FOOD_DETAIL_FRAGMENT_TAG = "FOOD_DETAIL_FRAGMENT_TAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.food_detail_activity_container) != null) {
            mTwoPane = true;

            //force only landscape orientation
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.food_detail_activity_container, new FoodDetailFragment(), FOOD_DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        Log.v(LOG_TAG, "twoPane MODE = " + mTwoPane);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                onRefreshAction();
                return true;
            case R.id.action_generate:
                onGenerateDataAction();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onRefreshAction() {
        updateFoodData();

//        FoodListFragment fragment = (FoodListFragment) getSupportFragmentManager().findFragmentById(R.id.main_list_container);
//        fragment.restartFoodLoader();
    }

    private void onGenerateDataAction() {

        FitFoodDbHelper dbHelper = new FitFoodDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (int i = 0; i < 10; i++) {
            ContentValues testValues = createSaladFoodValues(i);
            long rowId = db.insert(FitFoodContract.FoodEntry.TABLE_NAME, null, testValues);
            Log.d(LOG_TAG, "inserted rodID = " + rowId);
        }
        db.close();

        FoodListFragment fragment = (FoodListFragment) getSupportFragmentManager().findFragmentById(R.id.main_list_container);
        fragment.restartFoodLoader();
    }

    private void updateFoodData() {
        Log.v(LOG_TAG, "calling updateFoodData...");
        FetchFoodTask task = new FetchFoodTask(this);
        task.execute();
    }

    static ContentValues createSaladFoodValues(int i) {
        // Create a new map of values, where column names are the keys
        long actTime = (long) (new Date().getTime() + (Math.random() * 1000));
        ContentValues foodValues = new ContentValues();
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_AUTHOR, "Franta Pepa Jednička");
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_CREATED, actTime);
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_UPDATED, actTime);
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_NAME, "Test Salad " + i + ".");
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_TEXT, "How to make František's salads. " + i);
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_RATING, Math.min(Math.random() * 10, 10));
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_IMG, (byte[]) null);
        return foodValues;
    }

    /**
     * For calling activity when some detail is selected by listView
     * @param foodDetailUri detail uri
     */
    @Override
    public void onListItemSelected(Uri foodDetailUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(FoodDetailFragment.DETAIL_URI, foodDetailUri);

            FoodDetailFragment fragment = new FoodDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.food_detail_activity_container, fragment, FOOD_DETAIL_FRAGMENT_TAG)
                    .commit();

        } else {
            Log.d(LOG_TAG, "detail selected. URI = " + foodDetailUri.toString());
            Intent intent = new Intent(this, FoodDetailActivity.class).setData(foodDetailUri);
            startActivity(intent);
        }
    }
}
