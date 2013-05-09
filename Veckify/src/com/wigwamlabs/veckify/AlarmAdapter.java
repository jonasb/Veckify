package com.wigwamlabs.veckify;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.wigwamlabs.veckify.db.AlarmEntry;
import com.wigwamlabs.veckify.db.AlarmsCursor;

class AlarmAdapter extends CursorAdapter {
    private final Callback mCallback;

    public AlarmAdapter(Context context, Callback callback) {
        super(context, null, 0);
        mCallback = callback;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        view.setTag(new ViewHolder(view, mCallback));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final AlarmsCursor alarm = (AlarmsCursor) cursor;
        final ViewHolder vh = (ViewHolder) view.getTag();
        vh.update(alarm);
    }

    public interface Callback {
        void onAlarmEntryChanged(long alarmId, AlarmEntry entry);

        void onPickTime(long alarmId, AlarmEntry entry);

        void onPickPlaylist(long alarmId, AlarmEntry entry, String playlistLink);
    }

    private static class ViewHolder {
        private final Context mContext;
        private final Callback mCallback;
        private final TextView mTime;
        private final TextView mPlaylistName;
        private final Switch mEnabled;
        private final ImageButton mRepeatShuffleToggle;
        private long mAlarmId;
        private String mPlaylistLink;
        private int mHour;
        private int mMinute;
        private Pair<Intent, Intent> mIntents;
        private boolean mShuffle;
        private boolean mUpdating;
        private AlarmEntry mEntry;

        public ViewHolder(ViewGroup view, Callback callback) {
            mContext = view.getContext();
            mCallback = callback;

            // time
            mTime = (TextView) view.findViewById(R.id.time);
            mTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onPickTime(mAlarmId, mEntry);
                }
            });

            // playlist
            mPlaylistName = (TextView) view.findViewById(R.id.playlistName);
            mPlaylistName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onPickPlaylist(mAlarmId, mEntry, mPlaylistLink);
                }
            });

            // enabled
            mEnabled = (Switch) view.findViewById(R.id.enabled);
            mEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!mUpdating) {
                        mEntry.setEnabled(isChecked);
                        mCallback.onAlarmEntryChanged(mAlarmId, mEntry);
                    }
                }
            });

            // repeat/shuffle
            mRepeatShuffleToggle = (ImageButton) view.findViewById(R.id.repeatShuffleToggle);
            mRepeatShuffleToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEntry.setShuffle(!mShuffle);
                    mCallback.onAlarmEntryChanged(mAlarmId, mEntry);
                }
            });

            // run now
            view.findViewById(R.id.runNowButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startService(mIntents.first);
                    mContext.startActivity(mIntents.second);
                }
            });
        }

        public void update(AlarmsCursor alarm) {
            mUpdating = true;

            mAlarmId = alarm._id();
            // create an entry with all values needed to update the entry
            mEntry = new AlarmEntry();
            final boolean enabled = alarm.enabled();
            mEntry.setEnabled(enabled);
            mEntry.setTime(alarm.hour(), alarm.minute());
            mEntry.setRepeatDays(alarm.repeatDays());

            mHour = alarm.hour();
            mMinute = alarm.minute();
            mPlaylistLink = alarm.playlistLink();
            mShuffle = alarm.shuffle();
            mIntents = alarm.createIntents(mContext);

            mTime.setText(String.format("%d:%02d", mHour, mMinute));
            mPlaylistName.setText(alarm.playlistName());
            mEnabled.setChecked(enabled);
            mRepeatShuffleToggle.setImageResource(alarm.shuffle() ? R.drawable.ic_button_shuffle_inverse : R.drawable.ic_button_repeat_inverse);

            mUpdating = false;
        }
    }
}
