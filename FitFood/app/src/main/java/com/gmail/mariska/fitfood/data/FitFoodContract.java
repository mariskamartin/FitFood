package com.gmail.mariska.fitfood.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Contract for FitFood data
 */
public class FitFoodContract {
    public static final String CONTENT_AUTHORITY = "com.gmail.mariska.fitfood";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FOOD = "food";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

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

        public static String getFoodIdFromUri(Uri uri) {
            // food/*
            return uri.getPathSegments().get(1);
        }

        public static String getFoodSearchFromUri(Uri uri) {
            // food/search/*
            return uri.getPathSegments().get(2);
        }

        /**
         * Creates URI for concrete food
         * @param id id
         * @return uri
         */
        public static Uri buildFoodUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Build concrete food uri
         * @param foodId id
         * @return uri
         */
        public static Uri buildConcreteFood(String foodId) {
            return CONTENT_URI.buildUpon().appendPath(foodId).build();
        }

        /**
         * Build search uri
         * @param searchTxt txt
         * @return uri
         */
        public static Uri buildFoodSearch(String searchTxt) {
            return CONTENT_URI.buildUpon().appendPath("search").appendPath(searchTxt).build();
        }

        /**
         * Return all record of food
         * @return
         */
        public static Uri buildFoodAllUri() {
            return CONTENT_URI;
        }
    }

}
