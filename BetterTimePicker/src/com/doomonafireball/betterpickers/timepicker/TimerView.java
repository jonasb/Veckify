package com.doomonafireball.betterpickers.timepicker;

import com.doomonafireball.betterpickers.R;
import com.doomonafireball.betterpickers.ZeroTopPaddingTextView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class TimerView extends LinearLayout {

    private ZeroTopPaddingTextView mHoursOnes, mMinutesOnes;
    private ZeroTopPaddingTextView mHoursTens, mMinutesTens;

    private ZeroTopPaddingTextView mHoursSeperator;
    private ColorStateList mTextColor;

    public TimerView(Context context) {
        this(context, null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Init defaults
        mTextColor = getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
    }

    public void setTheme(int themeResId) {
        if (themeResId != -1) {
            TypedArray a = getContext().obtainStyledAttributes(themeResId, R.styleable.BetterPickersDialogFragment);

            mTextColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTextColor);
        }

        restyleViews();
    }

    private void restyleViews() {
        if (mHoursOnes != null) {
            mHoursOnes.setTextColor(mTextColor);
        }
        if (mMinutesOnes != null) {
            mMinutesOnes.setTextColor(mTextColor);
        }
        if (mHoursTens != null) {
            mHoursTens.setTextColor(mTextColor);
        }
        if (mMinutesTens != null) {
            mMinutesTens.setTextColor(mTextColor);
        }
        if (mHoursSeperator != null) {
            mHoursSeperator.setTextColor(mTextColor);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHoursTens = (ZeroTopPaddingTextView) findViewById(R.id.hours_tens);
        mMinutesTens = (ZeroTopPaddingTextView) findViewById(R.id.minutes_tens);
        mHoursOnes = (ZeroTopPaddingTextView) findViewById(R.id.hours_ones);
        mMinutesOnes = (ZeroTopPaddingTextView) findViewById(R.id.minutes_ones);
        mHoursSeperator = (ZeroTopPaddingTextView) findViewById(R.id.hours_seperator);
    }


    public void setTime(int hoursTensDigit, int hoursOnesDigit, int minutesTensDigit,
            int minutesOnesDigit) {
        if (mHoursTens != null) {
            // Hide digit
            if (hoursTensDigit == -2) {
                mHoursTens.setVisibility(View.INVISIBLE);
            } else if (hoursTensDigit == -1) {
                mHoursTens.setText("-");
                mHoursTens.setEnabled(false);
                mHoursTens.updatePadding();
                mHoursTens.setVisibility(View.VISIBLE);
            } else {
                mHoursTens.setText(String.format("%d", hoursTensDigit));
                mHoursTens.setEnabled(true);
                mHoursTens.updatePadding();
                mHoursTens.setVisibility(View.VISIBLE);
            }
        }
        if (mHoursOnes != null) {
            if (hoursOnesDigit == -1) {
                mHoursOnes.setText("-");
                mHoursOnes.setEnabled(false);
                mHoursOnes.updatePadding();
            } else {
                mHoursOnes.setText(String.format("%d", hoursOnesDigit));
                mHoursOnes.setEnabled(true);
                mHoursOnes.updatePadding();
            }
        }
        if (mMinutesTens != null) {
            if (minutesTensDigit == -1) {
                mMinutesTens.setText("-");
                mMinutesTens.setEnabled(false);
            } else {
                mMinutesTens.setEnabled(true);
                mMinutesTens.setText(String.format("%d", minutesTensDigit));
            }
        }
        if (mMinutesOnes != null) {
            if (minutesOnesDigit == -1) {
                mMinutesOnes.setText("-");
                mMinutesOnes.setEnabled(false);
            } else {
                mMinutesOnes.setText(String.format("%d", minutesOnesDigit));
                mMinutesOnes.setEnabled(true);
            }
        }
    }
}