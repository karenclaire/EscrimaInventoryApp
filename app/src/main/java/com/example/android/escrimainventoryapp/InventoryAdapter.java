package com.example.android.escrimainventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.escrimainventoryapp.data.InventoryContract.InventoryEntry;


/**
 * Created by karenulmer on 7/11/2017.
 */

public class InventoryAdapter extends CursorAdapter {


    public InventoryAdapter(MainActivity context, Cursor c) {
        super(context, c, 0);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Location of Views
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView totalSalesTextView = (TextView)view.findViewById(R.id.total_sales);
        ImageButton buy = (ImageButton) view.findViewById(R.id.buy);
        ImageView image = (ImageView) view.findViewById(R.id.default_image);


        //Read the attributes of the product from the Cursor for the current product.
        int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = (cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));
        final float price= cursor.getFloat(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE));
        final float totalSales = cursor.getFloat(cursor.getColumnIndex(InventoryEntry.COLUMN_TOTAL_SALES));

        image.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE))));
        final Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        final String productName = cursor.getString(nameColumnIndex);
        final int quantity= cursor.getInt(quantityColumnIndex);
        final String priceOutput = "Price per item" + price +"€";
        final String totalSalesOutput = "Total Remaining Inventory Value " + (price*quantity) + "€";


        // Update the TextViews with the attributes for the current item

        nameTextView.setText(productName);
        quantityTextView.setText(String.valueOf(quantity));
        priceTextView.setText(priceOutput);
        totalSalesTextView.setText(totalSalesOutput);


        // OnClicklistener for buy button
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                // If the quantity of the stock is greater than 0 we will subtract the quantity in 1 and add
                // to the sales list the price of the product each time the button is pressed,
                // finally update the fields.
                if (quantity > 0) {
                    int stock = quantity;
                    float pricePerItem = price;
                    float totalSales = (stock - 1) * pricePerItem;
                    values.put(InventoryEntry.COLUMN_PRICE, pricePerItem);
                    values.put(InventoryEntry.COLUMN_TOTAL_SALES, totalSales);
                    values.put(InventoryEntry.COLUMN_QUANTITY, -- stock);
                    resolver.update(
                            currentInventoryUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentInventoryUri, null);
                } else {
                    // No stock available
                    Toast.makeText(context, R.string.no_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}

