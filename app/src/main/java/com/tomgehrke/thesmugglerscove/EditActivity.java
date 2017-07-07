package com.tomgehrke.thesmugglerscove;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.tomgehrke.thesmugglerscove.data.InventoryContract.Item;

import java.io.ByteArrayOutputStream;

public class EditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    private Uri mCurrentItemUri;

    /* Track whether the user made a change */
    private boolean mItemHasChanged = false;

    /** EditText field to enter the item's name */
    private EditText mNameEditText;

    /** EditText field to enter the item's description */
    private EditText mDescriptionEditText;

    /** EditText field to enter the item's price */
    private EditText mPriceEditText;

    /** EditText field to enter the item's quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the item's supplier name */
    private EditText mSupplierNameEditText;

    /** EditText field to enter the item's supplier email */
    private EditText mSupplierEmailEditText;

    /** ImageView field to show item photo */
    private ImageView mItemPhotoImageView;

    /* Listener to check to see if a user change the item */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    // convert from bitmap to byte array
    private static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {return null;}
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    private static Bitmap getBitmapFromBytes(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mNameEditText.setOnTouchListener(mTouchListener);

        mDescriptionEditText = (EditText) findViewById(R.id.edit_item_description);
        mDescriptionEditText.setOnTouchListener(mTouchListener);

        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mPriceEditText.setOnTouchListener(mTouchListener);

        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        mSupplierNameEditText = (EditText) findViewById(R.id.edit_item_supplier_name);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);

        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_item_supplier_email);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);

        mItemPhotoImageView = (ImageView) findViewById(R.id.edit_item_photo);
        mItemPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        ImageButton mIncreaseQuantityButton = (ImageButton) findViewById(R.id.increase_quantity_button);
        mIncreaseQuantityButton.setOnTouchListener(mTouchListener);
        mIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = 0;
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    currentQuantity = Integer.valueOf(mQuantityEditText.getText().toString());
                }
                mQuantityEditText.setText(String.valueOf(currentQuantity + 1));
            }
        });

        ImageButton mDecreaseQuantityButton = (ImageButton) findViewById(R.id.decrease_quantity_button);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mDecreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = 0;
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    currentQuantity = Integer.valueOf(mQuantityEditText.getText().toString());
                }
                if (currentQuantity > 0) {
                    mQuantityEditText.setText(String.valueOf(currentQuantity - 1));
                }
            }
        });

        // Check to see if we're in Insert or Edit mode...
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            // No Uri was passed so we know we want to insert a new one
            setTitle(getString(R.string.edit_title_for_insert));
            invalidateOptionsMenu();
        } else {
            // Uri was passed so we know we want to edit an existing item
            setTitle(getString(R.string.edit_title_for_update));
            getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define projection
        String[] projection = new String[]{
                Item._ID,
                Item.COLUMN_ITEM_NAME,
                Item.COLUMN_ITEM_DESCRIPTION,
                Item.COLUMN_ITEM_PRICE,
                Item.COLUMN_ITEM_QUANTITY,
                Item.COLUMN_ITEM_SUPPLIER_NAME,
                Item.COLUMN_ITEM_SUPPLIER_EMAIL,
                Item.COLUMN_ITEM_IMAGE
        };

        return new android.support.v4.content.CursorLoader(
                this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(Item.COLUMN_ITEM_NAME));
            String description = data.getString(data.getColumnIndex(Item.COLUMN_ITEM_DESCRIPTION));
            String price = data.getString(data.getColumnIndex(Item.COLUMN_ITEM_PRICE));
            int quantity = data.getInt(data.getColumnIndex(Item.COLUMN_ITEM_QUANTITY));
            String supplierName = data.getString(data.getColumnIndex(Item.COLUMN_ITEM_SUPPLIER_NAME));
            String supplierEmail = data.getString(data.getColumnIndex(Item.COLUMN_ITEM_SUPPLIER_EMAIL));
            byte[] imageByteArray = data.getBlob(data.getColumnIndex(Item.COLUMN_ITEM_IMAGE));
            Bitmap image = null;

            if (imageByteArray != null) {
                image = getBitmapFromBytes(imageByteArray);
            }

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(String.valueOf(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierEmailEditText.setText(supplierEmail);
            mItemPhotoImageView.setImageBitmap(image);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText("0");
        mQuantityEditText.setText("0");
        mSupplierNameEditText.setText("");
        mSupplierEmailEditText.setText("");
        mItemPhotoImageView.setImageBitmap(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }

        // If there is no supplier information, hide the "Order more" menu item.
        if (mSupplierNameEditText.getText().toString().trim().isEmpty() ||
                mSupplierEmailEditText.getText().toString().trim().isEmpty()) {
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            orderMenuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (saveItem()) {
                    finish();
                }
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                DialogInterface.OnClickListener deleteButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteItem();
                                finish();
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showDeleteItemDialog(deleteButtonClickListener);
                return true;

            // Respond to a click on the "Remove Image" menu option
            case R.id.action_remove_image:
                mItemPhotoImageView.setImageBitmap(null);
                return true;

            // Respond to a click on the "Remove Image" menu option
            case R.id.action_order:
                orderItem();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
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

    private void showDeleteItemDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_item_prompt));
        builder.setPositiveButton(getString(R.string.delete_item_prompt_yes), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.delete_item_prompt_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep it" button, so dismiss the dialog
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.abandon_changes_prompt));
        builder.setPositiveButton(getString(R.string.abandon_changes_prompt_yes), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.abandon_changes_prompt_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

        StringBuilder errorMessages = new StringBuilder();
        boolean isSuccessful = true;

        // Load up what we've got
        String name = mNameEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        int quantity = 0;
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierEmail = mSupplierEmailEditText.getText().toString().trim();

        Bitmap image = null;
        if (mItemPhotoImageView.getDrawable() != null) {
            image = ((BitmapDrawable) mItemPhotoImageView.getDrawable()).getBitmap();
        }

        // Make sure we've got some basic valid entries before we continue

        if (name.isEmpty()) {
            errorMessages.append(getString(R.string.validation_empty_name));
        }
        if (supplierName.isEmpty()) {
            errorMessages.append(getString(R.string.validation_empty_supplier_name));
        }
        if (supplierEmail.isEmpty()) {
            errorMessages.append(getString(R.string.validation_empty_supplier_email));
        }

        if (errorMessages.length() > 0) {
            errorMessages.insert(0, getString(R.string.validation_error));
            Toast.makeText(this, errorMessages.toString(), Toast.LENGTH_SHORT).show();
            isSuccessful = false;
        }

        if (isSuccessful) {
            // Start loading values
            ContentValues itemValues = new ContentValues();

            // Name
            itemValues.put(Item.COLUMN_ITEM_NAME, name);

            // Description
            itemValues.put(Item.COLUMN_ITEM_DESCRIPTION, description);

            // Supplier Name
            itemValues.put(Item.COLUMN_ITEM_SUPPLIER_NAME, supplierName);

            // Supplier Email
            itemValues.put(Item.COLUMN_ITEM_SUPPLIER_EMAIL, supplierEmail);

            // Price
            if (TextUtils.isEmpty(price)) {
                price = "0";
            }
            itemValues.put(Item.COLUMN_ITEM_PRICE, price);

            // Quantity
            if (!TextUtils.isEmpty(mQuantityEditText.getText())) {
                quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            }
            if (quantity < 0) {quantity = 0;}
            itemValues.put(Item.COLUMN_ITEM_QUANTITY, quantity);

            // Photo
            itemValues.put(Item.COLUMN_ITEM_IMAGE, getBytesFromBitmap(image));

            // Validation and cleanup done. Let's write some data.
            if (mCurrentItemUri == null) {
                // No Uri means no item ID means we're inserting a new item
                Uri newUri = getContentResolver().insert(Item.CONTENT_URI, itemValues);

                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.insert_item_fail), Toast.LENGTH_SHORT).show();
                    isSuccessful = false;
                } else {
                    Toast.makeText(this, getString(R.string.insert_item_success), Toast.LENGTH_SHORT).show();
                }

            } else {
                // Uri means we have an item ID means we're updating an existing item
                long currentId = ContentUris.parseId(mCurrentItemUri);
                String[] selectionArgs = new String[]{String.valueOf(currentId)};
                String where = "_ID=?";

                int recordsUpdated = getContentResolver().update(
                        Item.CONTENT_URI,
                        itemValues,
                        where,
                        selectionArgs);

                if (recordsUpdated == 0) {
                    Toast.makeText(this, getString(R.string.update_item_fail), Toast.LENGTH_SHORT).show();
                    isSuccessful = false;
                } else {
                    Toast.makeText(this, getString(R.string.update_item_success), Toast.LENGTH_SHORT).show();
                }
            }
        }

        return isSuccessful;
    }

    private void deleteItem() {
        long currentId = ContentUris.parseId(mCurrentItemUri);
        String[] selectionArgs = new String[]{String.valueOf(currentId)};
        String where = "_ID=?";

        int recordsDeleted = getContentResolver().delete(
                Item.CONTENT_URI,
                where,
                selectionArgs);

        if (recordsDeleted == 0) {
            Toast.makeText(this, getString(R.string.delete_item_fail), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.delete_item_success), Toast.LENGTH_SHORT).show();
        }
    }

    private void orderItem() {
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierEmail = mSupplierEmailEditText.getText().toString().trim();
        String name = mNameEditText.getText().toString().trim();
        String subject = getString(R.string.order_subject);
        String body = getString(R.string.order_body, supplierName, name);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + supplierEmail));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mItemPhotoImageView.setImageBitmap(imageBitmap);
        }
    }
}
