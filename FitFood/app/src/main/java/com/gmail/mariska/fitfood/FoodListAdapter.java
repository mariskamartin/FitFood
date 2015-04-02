package com.gmail.mariska.fitfood;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * {@link com.gmail.mariska.fitfood.FoodListAdapter} exposes a list of foods
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class FoodListAdapter extends CursorAdapter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT, Locale.getDefault());

    public FoodListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Copy/paste note: Replace existing newView() method in ForecastAdapter with this one.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = R.layout.food_list_item;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        byte[] blob = cursor.getBlob(FoodListFragment.COL_FOOD_IMG);
        if(blob != null) {
            Bitmap bmp= BitmapFactory.decodeByteArray(blob, 0, blob.length);
            viewHolder.imgView.setImageBitmap(bmp);
        } else {
            viewHolder.imgView.setImageResource(R.drawable.noimage);
        }

        String foodName = cursor.getString(FoodListFragment.COL_FOOD_NAME);
        viewHolder.imgView.setContentDescription(foodName);
        viewHolder.foodNameView.setText(foodName);
        viewHolder.authorView.setText(cursor.getString(FoodListFragment.COL_FOOD_AUTHOR));
        viewHolder.ratingView.setText(String.valueOf(cursor.getInt(FoodListFragment.COL_FOOD_RATING)));
        viewHolder.dateView.setText(dateFormat.format(cursor.getLong(FoodListFragment.COL_FOOD_UPDATED)));
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView imgView;
        public final TextView foodNameView;
        public final TextView authorView;
        public final TextView ratingView;
        public final TextView dateView;

        public ViewHolder(View view) {
            imgView = (ImageView) view.findViewById(R.id.list_item_img);
            foodNameView = (TextView) view.findViewById(R.id.list_item_name_textview);
            authorView = (TextView) view.findViewById(R.id.list_item_author_textview);
            ratingView = (TextView) view.findViewById(R.id.list_item_rating_textview);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        }
    }
}