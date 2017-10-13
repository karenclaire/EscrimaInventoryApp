package com.example.android.escrimainventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by karenulmer on 7/11/2017.
 */

public class InventoryContract {


    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.escrimainventoryapp";

    /**
     * Base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_INVENTORY = "inventory";

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */

    public static final class InventoryEntry implements BaseColumns {

        /** The content URI to access the inventory data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        //Product Type, STRING, NOT NULL
        public static final String COLUMN_PRODUCT_TYPE = "product_type";
        //Material a product is made of, STRING, NOT NULL
        public static final String COLUMN_PRODUCT_MAKE = "product_make";
        //Product name or description, STRING, NOT NULL
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        // Product quantity, INTEGER, NOT NULL, DEFAULT 0
        public static final String COLUMN_QUANTITY = "quantity";
        // Product Price, FLOAT, NOT NULL
        public static final String COLUMN_PRICE = "price";
        //supplier contact name and contact details
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";
        public static final String COLUMN_SUPPLIER_CONTACT_NUMBER = "supplier_contact_number";
        // Total Sales, FLOAT
        public static final String COLUMN_TOTAL_SALES = "product_sales";
        //Image of item
        public static final String COLUMN_IMAGE = "image";


    }
}
