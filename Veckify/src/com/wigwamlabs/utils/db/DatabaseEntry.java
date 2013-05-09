package com.wigwamlabs.utils.db;

import android.content.ContentValues;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;

public class DatabaseEntry {
    private final String mTableName;
    protected final ContentValues mValues = new ContentValues();

    protected DatabaseEntry(String tableName) {
        mTableName = tableName;
    }

    public void update(SQLiteCursorLoader loader, long id) {
        loader.update(mTableName, mValues, "_id=" + id, null);
    }

    public void insert(SQLiteCursorLoader loader) {
        loader.insert(mTableName, null, mValues);
    }
}
