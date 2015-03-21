package com.gmail.mariska.fitfood.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for FitFood data
 */
public class FitFoodContract {
    public static final String CONTENT_AUTHORITY = "com.gmail.mariska.fitfood";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FOOD = "food";

    /**
     * Inner class that defines the table contents of the food table
     */
    public static final class FoodEntry implements BaseColumns {
        public static final String TABLE_NAME = "food";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_UPDATED = "updated";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_IMG = "image";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FOOD).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FOOD;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FOOD;

        //build URI methods
        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


}
