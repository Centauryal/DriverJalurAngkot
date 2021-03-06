package com.centaury.driverjalurangkot.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Centaury on 15/01/2018.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variable
    // Database version
    private static final int DATABASE_VERSION = 1;

    //Database name
    private static final String DATABASE_NAME = "driver_api";

    // Login table name
    private static final String TABLE_DRIVERS = "driver";

    // Login table columns name
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "nama";
    private static final String KEY_PHONE = "hp";
    private static final String KEY_PLAT = "nopol";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // creating table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_DRIVERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PHONE + " TEXT UNIQUE," + KEY_PLAT + " TEXT"
                + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database Table Created");

    }

    //upgrading table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRIVERS);

        // Create tables login
        onCreate(db);
    }

    /*
    Storing driver details in database
    */
    public void addDriver (String nama, String hp, String nopol) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, nama); //Name
        values.put(KEY_PHONE, hp); //Phone
        values.put(KEY_PLAT, nopol); //Platnomor

        // Inserting Row
        long id = db.insert(TABLE_DRIVERS, null, values);
        db.close();
        Log.d(TAG, "New driver inserted into sqlite: " + id);
    }

    /*
    Getting driver data from database
    */
    public HashMap<String, String > getDriverDetails(){
        HashMap<String, String > driver = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_DRIVERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0){
            driver.put("nama", cursor.getString(1));
            driver.put("hp", cursor.getString(2));
            driver.put("nopol", cursor.getString(3));
        }
        cursor.close();
        db.close();
        //return driver
        Log.d(TAG, "Fetching driver from Sqlite: " + driver.toString());

        return driver;
    }

    /*
    re create database delete all tables and create them again
    */
    public void deleteDrivers(){
        SQLiteDatabase db = this.getWritableDatabase();
        //delete all rows
        db.delete(TABLE_DRIVERS, null, null);
        db.close();

        Log.d(TAG, "deleted all driver info from sqlite");
    }
}
