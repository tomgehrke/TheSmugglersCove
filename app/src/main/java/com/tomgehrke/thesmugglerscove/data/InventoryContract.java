package com.tomgehrke.thesmugglerscove.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    // Content URI constants
    public static final String CONTENT_AUTHORITY = "com.tomgehrke.thesmugglerscove";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY_ITEM = Item.TABLE_NAME;

    /* Non-Instantiable */
    public InventoryContract() {}

    public static final class Item
            implements BaseColumns {

        /** Name of database table for items */
        public final static String TABLE_NAME = "item";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME = "name";
        public final static String COLUMN_ITEM_DESCRIPTION = "description";
        public final static String COLUMN_ITEM_PRICE = "price";
        public final static String COLUMN_ITEM_QUANTITY = "quantity";
        public final static String COLUMN_ITEM_IMAGE = "image";
        public final static String COLUMN_ITEM_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_ITEM_SUPPLIER_EMAIL = "supplier_email";

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY_ITEM);

        /** The MIME type of the {@link #CONTENT_URI} for a list of pets. */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_ITEM;

        /** The MIME type of the {@link #CONTENT_URI} for a single pet. */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_ITEM;
    }
}
