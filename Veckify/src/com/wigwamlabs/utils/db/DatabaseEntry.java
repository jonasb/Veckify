package com.wigwamlabs.utils.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.wigwamlabs.veckify.Debug;

public class DatabaseEntry implements Parcelable {
    protected final ContentValues mValues;
    private final String mTableName;

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

    public void update(SQLiteCursorLoader loader, long[] ids) {
        final StringBuilder sb = new StringBuilder();
        for (long id : ids) {
            if (sb.length() > 0) {
                sb.append(" OR ");
            }
            sb.append("_id=");
            sb.append(id);
        }
        loader.update(mTableName, mValues, sb.toString(), null);
    }

    public void insert(SQLiteCursorLoader loader) {
        loader.insert(mTableName, null, mValues);
    }

    public void insert(SQLiteDatabase db) {
        Debug.logSql("INSERT INTO " + mTableName + " VALUES " + mValues);
        db.insert(mTableName, null, mValues);
    }
}
