package com.wigwamlabs.veckify.db;

import com.wigwamlabs.utils.db.DatabaseEntry;

public class AlarmEntry extends DatabaseEntry {
    public AlarmEntry() {
        super(AlarmTable.n);
    }

    public void setEnabled(boolean enabled) {
        mValues.put(AlarmTable.enabled, enabled);
    }

    public void setTime(int hour, int minute) {
        mValues.put(AlarmTable.time, hour * 100 + minute);
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
}
