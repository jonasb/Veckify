package com.wigwamlabs.utils.db;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;

public class DatabaseEntry implements Parcelable {
    private final String mTableName;
    protected final ContentValues mValues;

    protected DatabaseEntry(String tableName) {
        mTableName = tableName;
        mValues = new ContentValues();
    }

    protected DatabaseEntry(String tableName, Parcel in) {
        mTableName = tableName;
        mValues = in.readParcelable(DatabaseEntry.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mValues, 0);
    }

    public ContentValues getValues() {
        return mValues;
    }

    protected void update(SQLiteCursorLoader loader, long id) {
        loader.update(mTableName, mValues, "_id=" + id, null);
    }

    public void insert(SQLiteCursorLoader loader) {
        loader.insert(mTableName, null, mValues);
    }
}
