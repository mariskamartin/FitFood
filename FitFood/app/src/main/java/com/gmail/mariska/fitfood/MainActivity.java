package com.gmail.mariska.fitfood;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.mariska.fitfood.data.FitFoodContract;
import com.gmail.mariska.fitfood.data.FitFoodDbHelper;

import java.util.Date;


/**
 * Main FIt Food activity.
 */
public class MainActivity extends ActionBarActivity implements FoodListFragment.Callback {

    private static final String FOOD_LIST_FRAGMENT_TAG = "FOOD_LIST_FRAGMENT_TAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FoodListFragment(), FOOD_LIST_FRAGMENT_TAG)
                    .commit();
        }
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void onRefreshAction() {
        FitFoodDbHelper dbHelper = new FitFoodDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (int i = 0; i < 10; i++) {
            ContentValues testValues = createSaladFoodValues(i);
            long rowId = db.insert(FitFoodContract.FoodEntry.TABLE_NAME, null, testValues);
            Log.d(LOG_TAG, "inserted rodID = " + rowId);
        }
        db.close();

        FoodListFragment fragment = (FoodListFragment) getSupportFragmentManager().findFragmentByTag(FOOD_LIST_FRAGMENT_TAG);
        fragment.restartFoodLoader();
    }
    static ContentValues createSaladFoodValues(int i) {
        // Create a new map of values, where column names are the keys
        long actTime = (long) (new Date().getTime() + (Math.random() * 1000));
        ContentValues foodValues = new ContentValues();
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_AUTHOR, "Martin M.");
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_CREATED, actTime);
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_UPDATED, actTime);
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_NAME, "Greek Salad " + i + ".");
        foodValues.put(FitFoodContract.FoodEntry.COLUMN_TEXT, "How to make salads. " + i);
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
        Log.d(LOG_TAG, "detail selected. URI = " + foodDetailUri.toString());
        Intent intent = new Intent(this, FoodDetailActivity.class).setData(foodDetailUri);
        startActivity(intent);
    }
}
