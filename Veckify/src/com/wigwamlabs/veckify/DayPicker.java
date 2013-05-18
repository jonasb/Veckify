package com.wigwamlabs.veckify;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static com.wigwamlabs.veckify.AlarmUtils.DAYS_NONE;

public class DayPicker extends View {
    private final Paint mTextPaint = new Paint();
    private final Paint mPressedPaint = new Paint();
    private final Paint mIndicatorPaint = new Paint();
    private final String[] mDayNames;
    private int mDays = DAYS_NONE;
    private int mPressedDay = DAYS_NONE;
    private OnDaysChangedListener mListener;
    private int mIndicatorHeight;
    private boolean mPressEnables;

    public DayPicker(Context context) {
        this(context, null, 0);
    }

    public DayPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DayPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final Resources res = context.getResources();
        mDayNames = res.getStringArray(R.array.repeatdays_shortday);
        for (int i = 0; i < mDayNames.length; i++) {
            mDayNames[i] = mDayNames[i].toUpperCase();
        }

        mTextPaint.setTextSize(res.getDimensionPixelSize(R.dimen.daypicker_textsize));
        mTextPaint.setColor(res.getColor(android.R.color.white));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.ITALIC));

        mIndicatorPaint.setColor(res.getColor(R.color.accent_primary));
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorHeight = res.getDimensionPixelSize(R.dimen.daypicker_indicatorheight);

        mPressedPaint.setColor(res.getColor(R.color.accent_secondary));
        mPressedPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            final int day = mapEventToDay(event);
            mPressedDay = day;
            mPressEnables = !isDaySet(day);
            setDay(day, mPressEnables, true);
            return true;
        }
        case MotionEvent.ACTION_MOVE: {
            final int day = mapEventToDay(event);
            if (day != mPressedDay) {
                mPressedDay = day;
                setDay(day, mPressEnables, true);
                invalidate();
            }
            return true;
        }
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP: {
            mPressedDay = DAYS_NONE;
            invalidate();
            return true;
        }
        }
        return false;
    }

    private int mapEventToDay(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final int width = getWidth();
        final int height = getHeight();

        if (x < 0 || x > width ||
                y < 0 || y > height) {
            return DAYS_NONE;
        }

        final float dayWidth = (float) width / mDayNames.length;
        int index = (int) (x / dayWidth);
        if (index >= mDayNames.length) {
            index = mDayNames.length - 1;
        }

        return (1 << index);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float dayWidth = (float) getWidth() / mDayNames.length;
        final int height = getHeight();

        //TODO ensure days follow locale's first weekday: Calendar.getFirstDayOfWeek()

        for (int i = 0; i < mDayNames.length; i++) {
            final int day = (1 << i);
            float backgroundHeight = height;
            final float left = i * dayWidth;
            final float right = (i + 1) * dayWidth;
            if ((mDays & day) != 0) {
                canvas.drawRect(left, height - mIndicatorHeight, right, height, mIndicatorPaint);
                backgroundHeight -= mIndicatorHeight;
            }
            if ((mPressedDay & day) != 0) {
                canvas.drawRect(left, 0, right, backgroundHeight, mPressedPaint);
            }
        }

        final float textY = height / 2 + mTextPaint.getTextSize() / 2;
        for (int i = 0; i < mDayNames.length; i++) {
            canvas.drawText(mDayNames[i], (i + 0.5f) * dayWidth, textY, mTextPaint);
        }
    }

    private void setDay(int day, boolean checked, boolean fromUser) {
        final int days;
        if (checked) {
            days = mDays | day;
        } else {
            days = mDays & ~day;
        }
        setDays(days, fromUser);
    }

    private boolean isDaySet(int day) {
        return (mDays & day) != 0;
    }

    private void setDays(int days, boolean fromUser) {
        if (days != mDays) {
            mDays = days;
            invalidate();

            if (mListener != null) {
                mListener.onDaysChanged(this, mDays, fromUser);
            }
        }
    }

    public int getDays() {
        return mDays;
    }

    public void setDays(int days) {
        setDays(days, false);
    }

    public void setDaysChangedListener(OnDaysChangedListener daysChangedListener) {
        mListener = daysChangedListener;
    }

    public interface OnDaysChangedListener {
        public void onDaysChanged(DayPicker dayPicker, int days, boolean fromUser);
    }
}
