package com.wigwamlabs.veckify.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.wigwamlabs.utils.db.DatabaseEntry;
import com.wigwamlabs.veckify.AlarmUtils;
import com.wigwamlabs.veckify.Debug;

import java.util.Calendar;

public class AlarmEntry extends DatabaseEntry {
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                @Override
                public AlarmEntry createFromParcel(Parcel in) {
                    return new AlarmEntry(in);
                }

                @Override
                public AlarmEntry[] newArray(int size) {
                    return new AlarmEntry[size];
                }
            };
    private boolean mHasPlaylist;

    public AlarmEntry() {
        super(AlarmTable.n);
    }

    public AlarmEntry(Parcel in) {
        super(AlarmTable.n, in);
        mHasPlaylist = in.readInt() > 0;
    }

    public static AlarmEntry createNew() {
        final AlarmEntry entry = new AlarmEntry();
        entry.setEnabled(false);
        entry.setRepeatDays(AlarmUtils.DAYS_NONE);
        entry.setVolume(100);
        entry.setShuffle(true);
        return entry;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mHasPlaylist ? 1 : 0);
    }

    public void setEnabled(boolean enabled) {
        mValues.put(AlarmTable.enabled, enabled);
    }

    public void setTime(int hour, int minute) {
        mValues.put(AlarmTable.time, hour * 100 + minute);
    }

    public Integer getTime() {
        return mValues.getAsInteger(AlarmTable.time);
    }

    public void setTime(Integer time) {
        mValues.put(AlarmTable.time, time);
    }

    public int getHour() {
        return mValues.getAsInteger(AlarmTable.time).intValue() / 100;
    }

    public int getMinute() {
        return mValues.getAsInteger(AlarmTable.time).intValue() % 100;
    }

    public void setRepeatDays(int repeatDays) {
        mValues.put(AlarmTable.repeatdays, repeatDays);
    }

    public void setOneoffTimeMs(long oneoffTimeMs) {
        mValues.put(AlarmTable.oneofftime_ms, oneoffTimeMs);
    }

    public void setHasPlaylist(boolean hasPlaylist) {
        // don't store it in the values... it's enough to know that we have a playlist
        mHasPlaylist = hasPlaylist;
    }

    public boolean hasPlaylist() {
        return mHasPlaylist || mValues.getAsString(AlarmTable.playlistlink) != null;
    }

    public void setPlaylistName(String playlistName) {
        mValues.put(AlarmTable.playlistname, playlistName);
    }

    public void setPlaylistLink(String playlistLink) {
        mValues.put(AlarmTable.playlistlink, playlistLink);
    }

    public void setVolume(int volume) {
        mValues.put(AlarmTable.volume, volume);
    }

    public void setShuffle(boolean shuffle) {
        mValues.put(AlarmTable.shuffle, shuffle);
    }

    @Override
    public void update(SQLiteCursorLoader loader, long id) {
        updateBeforeSaving(System.currentTimeMillis());

        super.update(loader, id);
    }

    void updateBeforeSaving(long nowMs) {
        final Boolean enabled = mValues.getAsBoolean(AlarmTable.enabled);
        if (enabled == null) {
            throw new RuntimeException("Didn't set value 'enabled'");
        }
        final Integer repeatDays = mValues.getAsInteger(AlarmTable.repeatdays);
        final Integer time = mValues.getAsInteger(AlarmTable.time);
        if (time == null) {
            throw new RuntimeException("Didn't set value 'time'");
        }
        if (repeatDays == null) {
            throw new RuntimeException("Didn't set value 'repeatdays'");
        }

        if (repeatDays.intValue() == AlarmUtils.DAYS_NONE) {
            final int hour = time.intValue() / 100;
            final int minute = time.intValue() % 100;
            final Calendar nextAlarmTime = AlarmUtils.getNextAlarmTime(enabled.booleanValue(), hour, minute, repeatDays.intValue(), 0, nowMs);
            setOneoffTimeMs(nextAlarmTime == null ? 0 : nextAlarmTime.getTimeInMillis());
        }
    }

    public void updateExpiredOneoffAlarms(DataDatabaseAdapter db) {
        final String where = String.format("%s = %d AND %s < %d", AlarmTable.repeatdays, AlarmUtils.DAYS_NONE, AlarmTable.oneofftime_ms, System.currentTimeMillis());
        final int updated = db.getWritableDatabase().update(AlarmTable.n, mValues, where, null);

        Debug.logSql("UPDATE " + AlarmTable.n + " VALUES " + mValues + " WHERE " + where + " => affected " + updated + " rows");
    }
}
