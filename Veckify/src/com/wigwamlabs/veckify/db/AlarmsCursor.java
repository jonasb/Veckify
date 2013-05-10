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
import com.wigwamlabs.veckify.AlarmUtils;
import com.wigwamlabs.veckify.Debug;
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
    private static final String QUERY_SELECT_ALL = SQLiteQueryBuilder.buildQueryString(false, AlarmTable.n, COLUMNS, AlarmTable.deleted + "=0", null, null, null, null);

    public AlarmsCursor(SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
        super(masterQuery, editTable, query);
    }

    public static SQLiteCursorLoader getAllAlarmsLoader(Context context, DataDatabaseAdapter db) {
        return new SQLiteCursorLoader(context, db, FACTORY, QUERY_SELECT_ALL, null);
    }

    public static AlarmsCursor getAllAlarmsCursor(DataDatabaseAdapter db) {
        Debug.logSql(QUERY_SELECT_ALL);
        return (AlarmsCursor) db.getReadableDatabase().rawQueryWithFactory(FACTORY, QUERY_SELECT_ALL, null, null);
    }

    public static AlarmsCursor getAlarmCursor(DataDatabaseAdapter db, long alarmId) {
        final String query = SQLiteQueryBuilder.buildQueryString(false, AlarmTable.n, COLUMNS, AlarmTable._id + " = " + alarmId, null, null, null, "1");
        Debug.logSql(query);
        return (AlarmsCursor) db.getReadableDatabase().rawQueryWithFactory(FACTORY, query, null, null);
    }

    public long _id() {
        return getLong(_id_index);
    }

    public boolean enabled() {
        final boolean enabled = getInt(enabled_index) > 0;
        if (enabled && repeatDays() == AlarmUtils.DAYS_NONE) { // one-off
            final long oneoffTime = oneoffTimeMs();
            if (oneoffTime > 0 && oneoffTime < System.currentTimeMillis()) {
                Debug.logAlarmScheduling("Alarm " + _id() + " out of synch (one-off event which has expired)");
                // db out of sync, should be fixed soon
                return false;
            }
        }
        return enabled;
    }

    public Integer time() {
        if (isNull(time_index)) {
            return null;
        }
        return getInt(time_index);
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
