package com.gmail.mariska.fitfood.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.gmail.mariska.fitfood.data.FitFoodContract.FoodEntry;

/**
 * Creation/Deletion of database
 */
public class FitFoodDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "fitfood.db";

    public FitFoodDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + FoodEntry.TABLE_NAME + " (" +
                FoodEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FoodEntry.COLUMN_NAME+ " TEXT NOT NULL, " +
                FoodEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                FoodEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                FoodEntry.COLUMN_CREATED + " INTEGER NOT NULL, " +
                FoodEntry.COLUMN_UPDATED + " INTEGER NOT NULL, " +
                FoodEntry.COLUMN_RATING + " INTEGER, " +
                FoodEntry.COLUMN_IMG + " BLOB " +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FoodEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
