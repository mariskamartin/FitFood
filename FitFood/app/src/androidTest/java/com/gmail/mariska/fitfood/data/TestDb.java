/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gmail.mariska.fitfood.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.gmail.mariska.fitfood.data.FitFoodContract.FoodEntry;

import java.util.HashSet;

/**
 * Test create and delete database
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(FitFoodDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }


    /**
     * Test create table
     * @throws Throwable
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(FitFoodContract.FoodEntry.TABLE_NAME);

        mContext.deleteDatabase(FitFoodDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new FitFoodDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        assertTrue("Error: Your database was created without all tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + FitFoodContract.FoodEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> foodColumnHashSet = new HashSet<String>();
        foodColumnHashSet.add(FoodEntry._ID);
        foodColumnHashSet.add(FoodEntry.COLUMN_AUTHOR);
        foodColumnHashSet.add(FoodEntry.COLUMN_CREATED);
        foodColumnHashSet.add(FoodEntry.COLUMN_UPDATED);
        foodColumnHashSet.add(FoodEntry.COLUMN_NAME);
        foodColumnHashSet.add(FoodEntry.COLUMN_TEXT);
        foodColumnHashSet.add(FoodEntry.COLUMN_RATING);
        foodColumnHashSet.add(FoodEntry.COLUMN_IMG);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            foodColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required food entry columns",
                foodColumnHashSet.isEmpty());
        db.close();
    }

    /**
     * Test Food Table
     */
    public long testFoodTable() {
        // First step: Get reference to writable database
        FitFoodDbHelper dbHelper = new FitFoodDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        ContentValues testExpectedValues = TestUtilities.createSaladFoodValues();

        // Insert ContentValues into database and get a row ID back
        long foodRowId;
        foodRowId = db.insert(FoodEntry.TABLE_NAME, null, testExpectedValues);
        // test id
        assertTrue(foodRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(FoodEntry.TABLE_NAME, null, null, null, null, null, null);

        // Move the cursor to a valid database row
        assertTrue( "Cursor is not valid. Has no rows", cursor.moveToFirst() );

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Location Query Validation Failed", cursor, testExpectedValues);
        assertFalse( "There should not be more than one record in query", cursor.moveToNext() );

        // Finally, close the cursor and database
        cursor.close();
        db.close();
        // Return the rowId of the inserted location, or "-1" on failure.
        return foodRowId;
    }
}
