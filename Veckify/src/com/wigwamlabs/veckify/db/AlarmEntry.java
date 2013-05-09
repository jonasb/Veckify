package com.wigwamlabs.veckify.db;

import android.content.ContentValues;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.wigwamlabs.utils.db.DatabaseEntry;
import com.wigwamlabs.veckify.alarms.Alarm;

public class AlarmEntry extends DatabaseEntry {
    public AlarmEntry() {
        super(AlarmTable.n);
    }

    public AlarmEntry(ContentValues values) {
        super(AlarmTable.n, values);
    }

    public void setEnabled(boolean enabled) {
        mValues.put(AlarmTable.enabled, enabled);
    }

    public void setTime(int hour, int minute) {
        mValues.put(AlarmTable.time, hour * 100 + minute);
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
        if (!enabled.booleanValue()) {
            return;
        }

        final Integer repeatDays = mValues.getAsInteger(AlarmTable.repeatdays);
        if (repeatDays == null) {
            throw new RuntimeException("Didn't set value 'repeatdays'");
        }
        if (repeatDays.intValue() == Alarm.DAYS_NONE) {
            final Integer time = mValues.getAsInteger(AlarmTable.time);
            if (time == null) {
                throw new RuntimeException("Didn't set value 'time'");
            }
            final int hour = time.intValue() / 100;
            final int minute = time.intValue() % 100;
            setOneoffTimeMs(Alarm.getNextAlarmTime(hour, minute, repeatDays.intValue(), nowMs).getTimeInMillis());
        }
    }
}
