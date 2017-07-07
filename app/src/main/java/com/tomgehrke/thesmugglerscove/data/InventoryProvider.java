package com.tomgehrke.thesmugglerscove.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tomgehrke.thesmugglerscove.data.InventoryContract.Item;

public class InventoryProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    /** URI matcher code for the content URI for the item table */
    private static final int ITEMS = 100;
    /** URI matcher code for the content URI for a single item in the item table */
    private static final int ITEM_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY_ITEM, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY_ITEM + "/#", ITEM_ID);
    }

    // Create global instance of the Inventory database helper
    private InventoryDbHelper mDbHelper;

    /** Initialize the provider and the database helper object. */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Implement this to handle query requests from clients.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * Example client call:<p>
     * <pre>// Request a specific record.
     * Cursor managedCursor = managedQuery(
     * ContentUris.withAppendedId(Contacts.People.CONTENT_URI, 2),
     * projection,    // Which columns to return.
     * null,          // WHERE clause.
     * null,          // WHERE clause value substitution
     * People.NAME + " ASC");   // Sort order.</pre>
     * Example implementation:<p>
     * <pre>// SQLiteQueryBuilder is a helper class that creates the
     * // proper SQL syntax for us.
     * SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
     *
     * // Set the table we're querying.
     * qBuilder.setTables(DATABASE_TABLE_NAME);
     *
     * // If the query ends in a specific record number, we're
     * // being asked for a specific record, so set the
     * // WHERE clause in our query.
     * if((URI_MATCHER.match(uri)) == SPECIFIC_MESSAGE){
     * qBuilder.appendWhere("_id=" + uri.getPathLeafId());
     * }
     *
     * // Make the query.
     * Cursor c = qBuilder.query(mDb,
     * projection,
     * selection,
     * selectionArgs,
     * groupBy,
     * having,
     * sortOrder);
     * c.setNotificationUri(getContext().getContentResolver(), uri);
     * return c;</pre>
     *
     * @param uri           The URI to query. This will be the full URI sent by the client;
     *                      if the client is requesting a specific record, the URI will end in a record number
     *                      that the implementation should parse and add to a WHERE or HAVING clause, specifying
     *                      that _id value.
     * @param projection    The list of columns to put into the cursor. If
     *                      {@code null} all columns are included.
     * @param selection     A selection criteria to apply when filtering rows.
     *                      If {@code null} then all rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the selection.
     *                      The values will be bound as Strings.
     * @param sortOrder     How the rows in the cursor should be sorted.
     *                      If {@code null} then the provider is free to define the sort order.
     * @return a Cursor or {@code null}.
     */
    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // For the ITEMS code, query the item table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the item table.
                cursor = database.query(Item.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.tomgehrke.thesmugglerscove.item/item/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = Item._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(Item.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     *
     * @param uri    The content:// URI of the insertion request. This must not be {@code null}.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The URI for the newly inserted item.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                Uri newUri = insertItem(uri, values);

                if (newUri != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return newUri;

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // DATA VALIDATION
        // Check name
        String name = values.getAsString(Item.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Items require a name");
        }

        // Check supplier name
        String supplierName = values.getAsString(Item.COLUMN_ITEM_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Items require a supplier");
        }

        // Check supplier email address
        String supplierEmail = values.getAsString(Item.COLUMN_ITEM_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Items require a supplier email address");
        }

        // DATA GOOD! Let's go!
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(Item.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Implement this to handle requests to delete one or more rows.
     * The implementation should apply the selection clause when performing
     * deletion, allowing the operation to affect multiple rows in a directory.
     * <p>
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * The implementation is responsible for parsing out a row ID at the end
     * of the URI, if a specific row is being deleted. That is, the client would
     * pass in <code>content://contacts/people/22</code> and the implementation is
     * responsible for parsing the record number (22) when creating a SQL statement.
     *
     * @param uri           The full URI to query, including a row ID (if a specific record is requested).
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs
     * @return The number of rows affected.
     */
    @Override
    public int delete(
            @NonNull Uri uri,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(Item.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;

            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = Item._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(Item.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Implement this to handle requests to update one or more rows.
     * The implementation should update all rows matching the selection
     * to set the columns according to the provided values map.
     * <p>
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     *
     * @param uri           The URI to query. This can potentially have a record ID if this
     *                      is an update request for a specific record.
     * @param values        A set of column_name/value pairs to update in the database.
     *                      This must not be {@code null}.
     * @param selection     An optional filter to match rows to update.
     * @param selectionArgs
     * @return the number of rows affected.
     */
    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                int itemsUpdated = updateItem(uri, values, selection, selectionArgs);

                if (itemsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }


                return itemsUpdated;

            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = Item._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri,
                           ContentValues values,
                           String selection,
                           String[] selectionArgs) {

        // DATA VALIDATION

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Check name
        if (values.containsKey(Item.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(Item.COLUMN_ITEM_NAME);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Items require a name");
            }
        }

        // Check supplier name
        if (values.containsKey(Item.COLUMN_ITEM_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(Item.COLUMN_ITEM_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Items require a supplier");
            }
        }

        // Check supplier email address
        if (values.containsKey(Item.COLUMN_ITEM_SUPPLIER_EMAIL)) {
            String supplierEmail = values.getAsString(Item.COLUMN_ITEM_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Items require a supplier email address");
            }
        }

        // DATA GOOD! Let's go!
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(
                Item.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Implement this to handle requests for the MIME type of the data at the
     * given URI.  The returned MIME type should start with
     * <code>vnd.android.cursor.item</code> for a single record,
     * or <code>vnd.android.cursor.dir/</code> for multiple items.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * <p>Note that there are no permissions needed for an application to
     * access this information; if your content provider requires read and/or
     * write permissions, or is not exported, all applications can still call
     * this method regardless of their access permissions.  This allows them
     * to retrieve the MIME type for a URI when dispatching intents.
     *
     * @param uri the URI to query.
     * @return a MIME type string, or {@code null} if there is no type.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return Item.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return Item.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
