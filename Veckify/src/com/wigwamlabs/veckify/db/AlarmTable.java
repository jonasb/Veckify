package com.wigwamlabs.veckify.db;

import android.database.sqlite.SQLiteDatabase;

import com.wigwamlabs.utils.db.QueryBuilder;

final class AlarmTable {
    static final String n = "Alarms";
    static final String _id = "_id";
    static final String deleted = "deleted";
    static final String enabled = "enabled";
    static final String time = "time";
    static final String repeatdays = "repeatdays";
    static final String oneofftime_ms = "oneofftime_ms";
    static final String playlistname = "playlistname";
    static final String playlistlink = "playlistlink";
    static final String volume = "volume";
    static final String shuffle = "shuffle";
    static final String telltime = "telltime";

    public static void create(SQLiteDatabase db) {
        // version 1
        QueryBuilder.create(n)
                .pk(_id)
                .integer(deleted)
                .integer(enabled)
                .integer(time, null)
                .integer(repeatdays)
                .integer(oneofftime_ms, null)
                .text(playlistname, null)
                .text(playlistlink, null)
                .integer(volume, null)
                .integer(shuffle)
        // version 2
                .integer(telltime, null)
                .execute(db);
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
        case 1:
            QueryBuilder.alterAddColumn(n, telltime)
                    .integer(null)
                    .execute(db);
            //fall-through
        }
    }
}
