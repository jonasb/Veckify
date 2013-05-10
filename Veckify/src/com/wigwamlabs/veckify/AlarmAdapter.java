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

import java.util.ArrayList;

class AlarmAdapter extends CursorAdapter {
    private final Callback mCallback;
    private final ArrayList<Long> mDeletedItems = new ArrayList<Long>();
    private boolean mEnablePlaylistPickers;

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
        int targetHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (isItemDeleted(alarm)) {
            targetHeight = 1;
        } else {
            final ViewHolder vh = (ViewHolder) view.getTag();
            vh.update(alarm, mEnablePlaylistPickers);
        }
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp.height != targetHeight) {
            lp.height = targetHeight;
            view.setLayoutParams(lp);
        }
    }

    private boolean isItemDeleted(AlarmsCursor alarm) {
        final long id = alarm._id();
        for (long deleted : mDeletedItems) {
            if (id == deleted) {
                return true;
            }
        }
        return false;
    }

    public void setEnablePlaylistPickers(boolean enable) {
        if (enable != mEnablePlaylistPickers) {
            mEnablePlaylistPickers = enable;
            notifyDataSetChanged();
        }
    }

    public void setItemsDeleted(long[] ids) {
        // we should eventually clear out the deleted items array,
        // but we don't expect too many deleted items so for consistency it's safer to keep it around
        for (long id : ids) {
            mDeletedItems.add(id);
        }
        notifyDataSetChanged();
    }

    public void setItemsUndeleted(long[] ids) {
        for (Long id : ids) {
            mDeletedItems.remove(id);
        }
        notifyDataSetChanged();
    }

    public interface Callback {
        void onAlarmEntryChanged(long alarmId, AlarmEntry entry, boolean enableIfPossible);

        void onPickTime(long alarmId, AlarmEntry entry);

        void onPickPlaylist(long alarmId, AlarmEntry entry, String playlistLink);

        void onPickRepeatDays(long alarmId, AlarmEntry entry);

        void onPickVolume(long alarmId, AlarmEntry entry);
    }

    private static class ViewHolder {
        private final Context mContext;
        private final Callback mCallback;
        private final TextView mTimeView;
        private final TextView mPlaylistName;
        private final Switch mEnabled;
        private final TextView mRepeatSchedule;
        private final ImageButton mRepeatShuffleToggle;
        private final ImageButton mVolumeButton;
        private final View mRunNowButton;
        private long mAlarmId;
        private String mPlaylistLink;
        private Integer mTime;
        private Pair<Intent, Intent> mIntents;
        private boolean mShuffle;
        private Integer mVolume;
        private boolean mUpdating;
        private AlarmEntry mEntry;

        public ViewHolder(ViewGroup view, Callback callback) {
            mContext = view.getContext();
            mCallback = callback;

            // time
            mTimeView = (TextView) view.findViewById(R.id.time);
            mTimeView.setOnClickListener(new View.OnClickListener() {
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
                        mCallback.onAlarmEntryChanged(mAlarmId, mEntry, false);
                    }
                }
            });

            // repeat schedule
            mRepeatSchedule = (TextView) view.findViewById(R.id.repeatSchedule);
            mRepeatSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onPickRepeatDays(mAlarmId, mEntry);
                }
            });

            // repeat/shuffle
            mRepeatShuffleToggle = (ImageButton) view.findViewById(R.id.repeatShuffleToggle);
            mRepeatShuffleToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEntry.setShuffle(!mShuffle);
                    mCallback.onAlarmEntryChanged(mAlarmId, mEntry, true);
                }
            });

            // volume
            mVolumeButton = (ImageButton) view.findViewById(R.id.volume);
            mVolumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEntry.setVolume(mVolume);
                    mCallback.onPickVolume(mAlarmId, mEntry);
                }
            });

            // run now
            mRunNowButton = view.findViewById(R.id.runNowButton);
            mRunNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startService(mIntents.first);
                    mContext.startActivity(mIntents.second);
                }
            });
        }

        public void update(AlarmsCursor alarm, boolean enablePlaylistPicker) {
            mUpdating = true;

            mAlarmId = alarm._id();
            mTime = alarm.time();
            mPlaylistLink = alarm.playlistLink();
            mShuffle = alarm.shuffle();
            mVolume = alarm.volume();
            final int repeatDays = alarm.repeatDays();
            mIntents = alarm.createIntents(mContext);
            final String playlist = alarm.playlistName();
            final boolean hasPlaylist = playlist != null && playlist.length() > 0;

            // create an entry with all values needed to update the entry
            mEntry = new AlarmEntry();
            final boolean enabled = alarm.enabled();
            mEntry.setEnabled(enabled);
            mEntry.setTime(mTime);
            mEntry.setRepeatDays(repeatDays);
            mEntry.setHasPlaylist(hasPlaylist);

            if (mTime != null) {
                //TODO am/pm
                mTimeView.setText(String.format("%d:%02d", mTime.intValue() / 100, mTime.intValue() % 100));
            } else {
                mTimeView.setText("-:--");
            }
            mPlaylistName.setText(hasPlaylist ? playlist : mContext.getText(R.string.noPlaylistSelected));
            mPlaylistName.setEnabled(enablePlaylistPicker);
            mEnabled.setChecked(enabled);
            mEnabled.setEnabled(hasPlaylist && mTime != null);
            mRepeatSchedule.setText(AlarmUtils.repeatDaysText(mContext, repeatDays));
            mRepeatShuffleToggle.setImageResource(alarm.shuffle() ? R.drawable.ic_button_shuffle_inverse : R.drawable.ic_button_repeat_inverse);
            mRunNowButton.setEnabled(hasPlaylist);

            mUpdating = false;
        }
    }
}
