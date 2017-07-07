package com.tomgehrke.thesmugglerscove;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tomgehrke.thesmugglerscove.data.InventoryContract.Item;

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    // convert from byte array to bitmap
    private static Bitmap getBitmapFromBytes(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.inventory_item, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name_textview);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_textview);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_textview);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.photo_imageview);
        Button sellButton = (Button) view.findViewById(R.id.sell_button);

        final Long id = cursor.getLong(cursor.getColumnIndex(Item._ID));
        String name = cursor.getString(cursor.getColumnIndex(Item.COLUMN_ITEM_NAME));
        final String quantity = cursor.getString(cursor.getColumnIndex(Item.COLUMN_ITEM_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndex(Item.COLUMN_ITEM_PRICE));
        byte[] imageByteArray = cursor.getBlob(cursor.getColumnIndex(Item.COLUMN_ITEM_IMAGE));

        nameTextView.setText(name);
        quantityTextView.setText(context.getString(R.string.item_label_quantity, quantity));
        priceTextView.setText(context.getString(R.string.item_label_price, price));

        if (imageByteArray != null) {
            photoImageView.setImageBitmap(getBitmapFromBytes(imageByteArray));
        } else {
            photoImageView.setVisibility(View.GONE);
        }

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.valueOf(quantity);
                if (currentQuantity > 0) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(Item.COLUMN_ITEM_QUANTITY, currentQuantity - 1);
                    String[] selectionArgs = new String[]{String.valueOf(id)};
                    String where = "_ID=?";

                    int recordsUpdated = context.getContentResolver().update(
                            Item.CONTENT_URI,
                            itemValues,
                            where,
                            selectionArgs);

                    if (recordsUpdated == 0) {
                        Toast.makeText(context, context.getString(R.string.sell_fail), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.sell_success), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
