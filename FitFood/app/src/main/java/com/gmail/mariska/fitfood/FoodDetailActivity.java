package com.gmail.mariska.fitfood;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


/**
 * Only for view a Food detail
 */
public class FoodDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putParcelable(FoodDetailFragment.DETAIL_URI, getIntent().getData());
            FoodDetailFragment fragment = new FoodDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.food_detail_activity_container, fragment)
                    .commit();
        }
    }

}
