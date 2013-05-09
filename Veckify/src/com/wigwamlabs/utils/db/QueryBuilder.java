package com.wigwamlabs.utils.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.wigwamlabs.veckify.Debug;

public final class QueryBuilder {
    private QueryBuilder() {
    }

    public static AlterQueryBuilder alterAddColumn(String tableName, String columnName) {
        return new AlterQueryBuilder(tableName, columnName);
    }

    public static CreateQueryBuilder create(String tableName) {
        return new CreateQueryBuilder(tableName, false);
    }

    public static CreateQueryBuilder createFts3(String tableName) {
        return new CreateQueryBuilder(tableName, true);
    }

    public static String drop(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public static ValuesBuilder values() {
        return new ValuesBuilder();
    }

    public static class AlterQueryBuilder {
        private final StringBuilder mQuery = new StringBuilder();

        public AlterQueryBuilder(String tableName, String columnName) {
            mQuery
                    .append("ALTER TABLE ")
                    .append(tableName)
                    .append(" ADD COLUMN ")
                    .append(columnName);
        }

        public void execute(SQLiteDatabase db) {
            Debug.logSql(mQuery.toString());
            db.execSQL(mQuery.toString());
        }

        public AlterQueryBuilder integer(Boolean nullable) {
            type(" INTEGER", nullable);
            return this;
        }

        public AlterQueryBuilder text(Boolean nullable) {
            type(" TEXT", nullable);
            return this;
        }

        private void type(String t, Boolean nullable) {
            mQuery.append(t);
            if (nullable != null) {
                mQuery.append(" NOT NULL");
            }
        }
    }

    public static class CreateQueryBuilder {
        private final StringBuilder mQuery = new StringBuilder();
        private boolean mHasAddedColumns;

        public CreateQueryBuilder(String tableName, boolean fts3) {
            if (fts3) {
                mQuery.append("CREATE VIRTUAL TABLE ");
                mQuery.append(tableName);
                mQuery.append(" USING fts3 (");
            } else {
                mQuery.append("CREATE TABLE ");
                mQuery.append(tableName);
                mQuery.append(" (");
            }
        }

        public void execute(SQLiteDatabase db) {
            mQuery.append(")");
            Debug.logSql(mQuery.toString());
            db.execSQL(mQuery.toString());
        }

        private void field(String columnName, String type, Boolean nullable) {
            prepareForColumn();
            mQuery.append(columnName);
            mQuery.append(type);
            if (nullable != null) {
                mQuery.append(" NOT NULL");
            }
        }

        public CreateQueryBuilder integer(String columnName) {
            field(columnName, " INTEGER", Boolean.FALSE);
            return this;
        }

        public CreateQueryBuilder integer(String columnName, Boolean nullable) {
            field(columnName, " INTEGER", nullable);
            return this;
        }

        private void prepareForColumn() {
            if (mHasAddedColumns) {
                mQuery.append(", ");
            }
            mHasAddedColumns = true;
        }

        public CreateQueryBuilder pk(String columnName) {
            prepareForColumn();
            mQuery.append(columnName);
            mQuery.append(" INTEGER PRIMARY KEY AUTOINCREMENT");
            return this;
        }

        public CreateQueryBuilder real(String columnName) {
            field(columnName, " REAL", Boolean.FALSE);
            return this;
        }

        public CreateQueryBuilder real(String columnName, Boolean nullable) {
            field(columnName, " REAL", nullable);
            return this;
        }

        public CreateQueryBuilder text(String columnName) {
            field(columnName, " TEXT", Boolean.FALSE);
            return this;
        }

        public CreateQueryBuilder text(String columnName, Boolean nullable) {
            field(columnName, " TEXT", nullable);
            return this;
        }
    }

    public static class ValuesBuilder {
        private final ContentValues mValues = new ContentValues();

        public ContentValues end() {
            return mValues;
        }

        public ValuesBuilder put(String key, Integer value) {
            mValues.put(key, value);
            return this;
        }

        public ValuesBuilder put(String key, Long value) {
            mValues.put(key, value);
            return this;
        }

        public ValuesBuilder put(String key, String value) {
            mValues.put(key, value);
            return this;
        }
    }
}
