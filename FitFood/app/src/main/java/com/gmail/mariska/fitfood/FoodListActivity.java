package com.gmail.mariska.fitfood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gmail.mariska.fitfood.sync.FitFoodSyncAdapter;


/**
 * Main FIt Food activity.
 */
public class FoodListActivity extends ActionBarActivity implements FoodListFragment.Callback {

    private static final String FOOD_DETAIL_FRAGMENT_TAG = "FOOD_DETAIL_FRAGMENT_TAG";
    private static final String LOG_TAG = FoodListActivity.class.getSimpleName();
    /**
     * Own private broadcast intent receiver
     */
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "BroadcastReceiver - onReceive");
            int newFoodCount = intent.getIntExtra(FitFoodSyncAdapter.SERVICE_FOODS_UPDATE_COUNT_KEY, 0);
            if (newFoodCount > 0) {
                // refresh the list
                FoodListFragment fragment = (FoodListFragment) getSupportFragmentManager().findFragmentById(R.id.main_list_container);
                fragment.restartFoodLoader();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.notification_sync_nodata), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private boolean mTwoPane = false;

    @Override
    protected void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        this.registerReceiver(myReceiver, new IntentFilter(FitFoodSyncAdapter.INTENT_FOOD_SERVICE_SYNC));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        this.unregisterReceiver(myReceiver);
        super.onPause();
    }

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

        FitFoodSyncAdapter.initializeSyncAdapter(this);

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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_refresh:
                onRefreshAction();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onRefreshAction() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.notification_user_start_sync), Toast.LENGTH_SHORT).show();
        FitFoodSyncAdapter.syncImmediately(this);
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
