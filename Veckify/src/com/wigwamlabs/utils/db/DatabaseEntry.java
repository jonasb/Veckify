package com.wigwamlabs.utils.db;

import android.content.ContentValues;
import android.os.Parcelable;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;

public class DatabaseEntry {
    private final String mTableName;
    protected final ContentValues mValues;

    protected DatabaseEntry(String tableName) {
        mTableName = tableName;
        mValues = new ContentValues();
    }

    public DatabaseEntry(String tableName, ContentValues values) {
        mTableName = tableName;
        mValues = values;
    }

    public ContentValues getValues() {
        return mValues;
    }

    public void update(SQLiteCursorLoader loader, long id) {
        loader.update(mTableName, mValues, "_id=" + id, null);
    }

    public void insert(SQLiteCursorLoader loader) {
        loader.insert(mTableName, null, mValues);
    }
}
