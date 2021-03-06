package com.gmail.mariska.fitfood.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.gmail.mariska.fitfood.data.FitFoodContract.FoodEntry;

/**
 * Provider for FitFood app
 */
public class FitFoodProvider extends ContentProvider {
    private static String LOG_TAG = FitFoodProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FitFoodDbHelper mOpenHelper;

    private static final int FOODS = 100;
    private static final int CONCRETE_FOOD = 101;
    private static final int SEARCH_FOODS = 102;

    private static final String sConcreteFoodSelection = FoodEntry.TABLE_NAME + "." + FoodEntry._ID + " = ? ";
    private static final String sFoodSearchSelection = "upper("+FoodEntry.TABLE_NAME + "." + FoodEntry.COLUMN_NAME + ") like upper(?) ";


    /**
     * Creates URI matcher for FitFood
     * @return uriMatcher
     */
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FitFoodContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FitFoodContract.PATH_FOOD, FOODS);
        matcher.addURI(authority, FitFoodContract.PATH_FOOD + "/#", CONCRETE_FOOD);
        matcher.addURI(authority, FitFoodContract.PATH_FOOD + "/search/*", SEARCH_FOODS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FitFoodDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CONCRETE_FOOD:
                //only this is for detail
                return FoodEntry.CONTENT_ITEM_TYPE;
            case FOODS:
                return FoodEntry.CONTENT_TYPE;
            case SEARCH_FOODS:
                return FoodEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            // "food/*"
            case CONCRETE_FOOD:
            {
                String foodId = FoodEntry.getFoodIdFromUri(uri);
                selectionArgs = new String[]{foodId};
                selection = sConcreteFoodSelection;

                retCursor = db.query(
                        FoodEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "food"
            case FOODS: {
                retCursor = db.query(
                        FoodEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "food/search/*"
            case SEARCH_FOODS: {
                String search = FoodEntry.getFoodSearchFromUri(uri);
                selectionArgs = new String[]{"%"+search+"%"}; //finds all contains search string
                selection = sFoodSearchSelection;

                retCursor = db.query(
                        FoodEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
//        db.close();
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FOODS: {
                normalizeDate(values);
                long _id = db.insert(FoodEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = FoodEntry.buildFoodUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int deletedRows;

        if (selection == null) selection = "1";

        switch (match) {
            case FOODS: {
                deletedRows = db.delete(FoodEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int updatedRows;

        if (selection == null) selection = "1";
        switch (match) {
            case FOODS: {
                normalizeDate(values);
                updatedRows = db.update(FoodEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (updatedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return updatedRows;
    }

    /**
     * normalize the date value
     * @param values data
     */
    private void normalizeDate(ContentValues values) {
        if (values.containsKey(FoodEntry.COLUMN_CREATED)) {
            long dateValue = values.getAsLong(FoodEntry.COLUMN_CREATED);
            values.put(FoodEntry.COLUMN_CREATED, FitFoodContract.normalizeDate(dateValue));

        } else if (values.containsKey(FoodEntry.COLUMN_UPDATED)) {
            long dateValue = values.getAsLong(FoodEntry.COLUMN_UPDATED);
            values.put(FoodEntry.COLUMN_UPDATED, FitFoodContract.normalizeDate(dateValue));
        }

    }

}
