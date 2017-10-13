package com.example.android.escrimainventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.escrimainventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by karenulmer on 7/11/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "escrimainventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db){
    // Create a String that contains the SQL statement to create the inventory table
    String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
            + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + InventoryEntry.COLUMN_PRODUCT_TYPE + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_MAKE + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
            + InventoryEntry.COLUMN_PRICE + " REAL NOT NULL DEFAULT 0.0, "
            + InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_SUPPLIER_CONTACT_NUMBER + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_TOTAL_SALES + " REAL NOT NULL DEFAULT 0.0, "
            + InventoryEntry.COLUMN_IMAGE + " BLOB NOT NULL DEFAULT 'no image' );";

    // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME);
        onCreate(db);

    }
}
