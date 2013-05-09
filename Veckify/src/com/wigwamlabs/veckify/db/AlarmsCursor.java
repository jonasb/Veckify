package com.wigwamlabs.veckify.db;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.wigwamlabs.spotify.PendingPlayPlaylistAction;
import com.wigwamlabs.spotify.SpotifyService;
import com.wigwamlabs.veckify.NowPlayingActivity;

public class AlarmsCursor extends SQLiteCursor {
    private static final SQLiteDatabase.CursorFactory FACTORY;

    static {
        FACTORY = new SQLiteDatabase.CursorFactory() {
            @Override
            public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
                return new AlarmsCursor(masterQuery, editTable, query);
            }
        };
    }

    private static final String[] COLUMNS = {
            AlarmTable._id,
            AlarmTable.enabled,
            AlarmTable.time,
            AlarmTable.repeatdays,
            AlarmTable.oneofftime_ms,
            AlarmTable.playlistname,
            AlarmTable.playlistlink,
            AlarmTable.volume,
            AlarmTable.shuffle,
    };
    private static final int _id_index = 0;
    private static final int enabled_index = 1;
    private static final int time_index = 2;
    private static final int repeatdays_index = 3;
    private static final int oneofftime_ms_index = 4;
    private static final int playlistname_index = 5;
    private static final int playlistlink_index = 6;
    private static final int volume_index = 7;
    private static final int shuffle_index = 8;

    public AlarmsCursor(SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
        super(masterQuery, editTable, query);
    }

    public static SQLiteCursorLoader getAllAlarms(Context context, DataDatabaseAdapter db) {
        final String query = SQLiteQueryBuilder.buildQueryString(false, AlarmTable.n, COLUMNS, null, null, null, null, null);
        return new SQLiteCursorLoader(context, db, FACTORY, query, null);
    }

    public long _id() {
        return getLong(_id_index);
    }

    public boolean enabled() {
        return getInt(enabled_index) > 0;
    }

    public int hour() {
        return getInt(time_index) / 100;
    }

    public int minute() {
        return getInt(time_index) % 100;
    }

    public int repeatDays() {
        return getInt(repeatdays_index);
    }

    public long oneoffTimeMs() {
        return getLong(oneofftime_ms_index);
    }

    public String playlistName() {
        return getString(playlistname_index);
    }

    public String playlistLink() {
        return getString(playlistlink_index);
    }

    public int volume() {
        return getInt(volume_index);
    }

    public boolean shuffle() {
        return getInt(shuffle_index) > 0;
    }

    public Pair<Intent, Intent> createIntents(Context context) {
        // setup intent to launch now playing
        final Intent nowPlayingIntent = new Intent(context, NowPlayingActivity.class);
        nowPlayingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // launch now playing in alarm mode
        nowPlayingIntent.setAction(NowPlayingActivity.ACTION_ALARM);

        // tell service to start playing
        final Intent intent = new Intent(context, SpotifyService.class);
        intent.setAction(SpotifyService.ACTION_PLAY_PLAYLIST);
        final boolean tellTime = true; //TODO make editable
        final PendingPlayPlaylistAction pendingAction = new PendingPlayPlaylistAction(playlistLink(), PendingIntent.getActivity(context, 0, nowPlayingIntent, 0), volume(), shuffle(), tellTime);
        intent.putExtra(SpotifyService.EXTRA_ACTION, pendingAction);

        return Pair.create(intent, nowPlayingIntent);
    }
}
