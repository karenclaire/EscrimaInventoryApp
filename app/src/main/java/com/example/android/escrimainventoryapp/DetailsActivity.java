package com.example.android.escrimainventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.escrimainventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.escrimainventoryapp.data.InventoryDbHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by karenulmer on 7/11/2017.
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int IMAGE_REQUEST = 0;
    /**
     * Identifier for the inventory data loader
     */
    private static final int EXISTING_INVENTORY_LOADER = 0;
    EditText productTypeEdit;
    EditText productMakeEdit;
    EditText productNameEdit;
    EditText priceEdit;
    EditText quantityEdit;
    EditText supplierNameEdit;
    EditText supplierPhoneEdit;
    EditText supplierEmailEdit;
    TextView totalSalesView;
    long currentItemId;
    ImageButton decreaseQuantity;
    ImageButton increaseQuantity;
    Button imageBtn;
    ImageView imageView;

    private InventoryDbHelper dbHelper;

    public String currentPhoto = "no image";

    //Image Uri
    private Uri mImageUri;
     /**
     * Content URI for the existing inventory (null if it's a new item)
     */
    private Uri mCurrentInventoryUri;

    /**
     * Boolean flag that keeps track of whet
     */
    private boolean mInventoryHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mInventoryHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        // If the intent DOES NOT contain a inventory content URI, then we know that we are
        // creating a new inventory.
        if (mCurrentInventoryUri == null)

        {
            // This is a new inventory, so change the app bar to say "Add New Item"
            setTitle(getString(R.string.add_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else

        {
            // Otherwise this is an existing pet, so change app bar to say "Edit Item"
            setTitle(getString(R.string.edit_item));

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }


        //  Find all relevant views that we will need to read user input from
        ScrollView scrollview = (ScrollView) findViewById(R.id.scroll_view);
        productTypeEdit = (EditText) findViewById(R.id.product_type_hint);
        productMakeEdit = (EditText) findViewById(R.id.product_make_hint);
        productNameEdit = (EditText) findViewById(R.id.product_name_hint);
        priceEdit = (EditText) findViewById(R.id.price_field);
        quantityEdit = (EditText) findViewById(R.id.quantity_field);
        supplierNameEdit = (EditText) findViewById(R.id.supplier_name_field);
        supplierPhoneEdit = (EditText) findViewById(R.id.supplier_contact_number_field);
        supplierEmailEdit = (EditText) findViewById(R.id.supplier_email_field);
        decreaseQuantity = (ImageButton) findViewById(R.id.decrease_quantity);
        increaseQuantity = (ImageButton) findViewById(R.id.increase_quantity);
        imageBtn = (Button) findViewById(R.id.select_image);
        imageView = (ImageView) findViewById(R.id.default_image);

        dbHelper = new InventoryDbHelper(this);

        currentItemId = getIntent().getLongExtra("itemId", 0);

        productTypeEdit.setOnTouchListener(mTouchListener);
        productMakeEdit.setOnTouchListener(mTouchListener);
        productNameEdit.setOnTouchListener(mTouchListener);
        priceEdit.setOnTouchListener(mTouchListener);
        quantityEdit.setOnTouchListener(mTouchListener);
        supplierNameEdit.setOnTouchListener(mTouchListener);
        supplierEmailEdit.setOnTouchListener(mTouchListener);
        supplierPhoneEdit.setOnTouchListener(mTouchListener);

        // Scroll view on top position
        scrollview.fullScroll(ScrollView.FOCUS_UP);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneToQuantity();
                mInventoryHasChanged = true;
            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sumOneToQuantity();
                mInventoryHasChanged = true;
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToOpenImageSelector();
                mInventoryHasChanged = true;
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void subtractOneToQuantity() {
        String previousValueString = quantityEdit.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            quantityEdit.setText(String.valueOf(previousValue - 1));
        }
    }

    private void sumOneToQuantity() {
        String previousValueString = quantityEdit.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        quantityEdit.setText(String.valueOf(previousValue + 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemId == 0) {
            MenuItem deleteOneItemMenuItem = menu.findItem(R.id.action_delete_item);
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            MenuItem addDummyMenuItem = menu.findItem(R.id.add_dummy_data);
            //deleteOneItemMenuItem.setVisible(false);
            //orderMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the save or check menu option
            case R.id.action_save:
                if (saveItem()) {
                    return true;
                }
            case android.R.id.home:
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            // dialog with phone and email
            case R.id.action_order:
                showOrderConfirmationDialog();
                return true;
            // Respond to a click on the "Delete item" menu option
            case R.id.action_delete_item:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_data:
                showDeleteConfirmationDialog();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all inventory attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_TYPE,
                InventoryEntry.COLUMN_PRODUCT_MAKE,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_EMAIL,
                InventoryEntry.COLUMN_SUPPLIER_CONTACT_NUMBER,
                InventoryEntry.COLUMN_TOTAL_SALES,
                InventoryEntry.COLUMN_IMAGE};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,   // Query the content URI for the current inventory
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
            int typeColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_TYPE);
            int makeColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_MAKE);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_EMAIL);
            int supplierContactNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_CONTACT_NUMBER);
            int totalSalesColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_TOTAL_SALES);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);


            // Extract out the value from the Cursor for the given column index
            String type = cursor.getString(typeColumnIndex);
            String make = cursor.getString(makeColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            String supplierPhone = cursor.getString(supplierContactNumberColumnIndex);
            float totalSales = cursor.getFloat(totalSalesColumnIndex);
            currentPhoto = cursor.getString(imageColumnIndex);
            mImageUri = Uri.parse(currentPhoto);

            /** Update the views on the screen with the values from the database /**/
            // Product type required
                productTypeEdit.setText(type);
                productMakeEdit.setText(make);
                productNameEdit.setText(name);
                quantityEdit.setText(Integer.toString(quantity));
                priceEdit.setText(Float.toString(price));
                supplierNameEdit.setText(supplierName);
                supplierEmailEdit.setText(supplierEmail);
                supplierPhoneEdit.setText(supplierPhone);



            ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    imageView.setImageBitmap(getBitmapFromUri(mImageUri));
                }

            //imageView.setImageURI(mImageUri);


                });
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productTypeEdit.setText("");
        productMakeEdit.setText("");
        productNameEdit.setText("");
        quantityEdit.setText("");
        priceEdit.setText("");
        supplierNameEdit.setText("");
        supplierEmailEdit.setText("");
        supplierPhoneEdit.setText("");
    }


    private void showOrderConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.order_message);
        builder.setPositiveButton(R.string.phone, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // intent to phone
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + supplierPhoneEdit.getText().toString().trim()));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.email, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // intent to email
                Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + supplierEmailEdit.getText().toString().trim()));
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Additional order");
                String bodyMessage = "Please send additional quantities of" +
                        productNameEdit.getText().toString().trim() +
                        ". Thank you!";
                intent.putExtra(android.content.Intent.EXTRA_TEXT, bodyMessage);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private boolean saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productType = productTypeEdit.getText().toString().trim();
        String productMake = productMakeEdit.getText().toString().trim();
        String productName = productNameEdit.getText().toString().trim();
        String quantityString = quantityEdit.getText().toString().trim();
        String priceString = priceEdit.getText().toString().trim();
        String supplierName = supplierNameEdit.getText().toString().trim();
        String supplierEmail = supplierEmailEdit.getText().toString().trim();
        String supplierContactNumber = supplierPhoneEdit.getText().toString().trim();


            // Create a ContentValues object where column names are the keys,
            // and inventory attributes from the editor are the values.
            ContentValues values = new ContentValues();
            if (!TextUtils.isEmpty(productType)) {
                values.put(InventoryEntry.COLUMN_PRODUCT_TYPE, productType);
            } else {
                Toast.makeText(this,  getResources().getString(R.string.type_require),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!TextUtils.isEmpty(productMake)) {
                values.put(InventoryEntry.COLUMN_PRODUCT_MAKE, productMake);
            } else {
                Toast.makeText(this,  getResources().getString(R.string.make_require),
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            if (!TextUtils.isEmpty(productName)) {
                values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
            } else {
                Toast.makeText(this, getResources().getString(R.string.name_require),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!TextUtils.isEmpty(quantityString)){
            Integer quantity = Integer.parseInt(quantityString);
                if (quantity >= 0 )
                values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
            } else {
                Toast.makeText(this,  getResources().getString(R.string.quantity_require),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!TextUtils.isEmpty(priceString)){
            Float price = Float.parseFloat(priceString);
                if (price >= 0)
                values.put(InventoryEntry.COLUMN_PRICE, price);
            } else {
            Toast.makeText(this, getResources().getString(R.string.price_require),
                    Toast.LENGTH_SHORT).show();
            return true;
            }
            if (!TextUtils.isEmpty(supplierName)) {
                values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierName);
            } else {
                Toast.makeText(this,  getResources().getString(R.string.supplier_name_require),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!TextUtils.isEmpty(supplierEmail)) {
                values.put(InventoryEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);
            } else {
                Toast.makeText(this,  getResources().getString(R.string.supplier_email_require),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!TextUtils.isEmpty(supplierContactNumber)) {
                values.put(InventoryEntry.COLUMN_SUPPLIER_CONTACT_NUMBER, supplierContactNumber);
            } else {
                Toast.makeText(this, getResources().getString(R.string.supplier_phone_require),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (mImageUri != null) {
                values.put(InventoryEntry.COLUMN_IMAGE, String.valueOf(mImageUri));
            } else {
                Toast.makeText(this, getResources().getString(R.string.image_required), Toast.LENGTH_SHORT).show();
                return true;
            }

            // Determine if this is a new or existing item  by checking if mCurrentInventoryUri is null or not
            if (mCurrentInventoryUri == null) {
                // This is a NEW inventory, so insert a new item into the provider,
                // returning the content URI for the new item.
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.insert_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.insert_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING Inventory, so update the inventory with content URI: mCurrentInventoryUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentInventoryUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.update_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.update_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }

        finish();
        return  true;
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventoryUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.update_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    public void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted

                }
            }
        }
    }


        @Override
        public void onActivityResult ( int requestCode, int resultCode, Intent resultData){
            // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
            // If the request code seen here doesn't match, it's the response to some other intent,
            // and the below code shouldn't run at all.

            if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
                try {
                    mImageUri = resultData.getData();
                    Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                    int takeFlags = resultData.getFlags();
                    takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }


                    imageView.setImageBitmap(getBitmapFromUri(mImageUri));
                    Log.i(LOG_TAG, "image works");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }


    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

}
