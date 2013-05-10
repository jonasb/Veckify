/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// The animation code comes from SwipeDismissListViewTouchListener.java from DashClock
// https://code.google.com/p/dashclock/

package com.wigwamlabs.veckify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.systemui.SwipeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwipeDismissListView extends ListView implements SwipeHelper.Callback {
    private final SwipeHelper mSwipeHelper;
    private final int mAnimationTime;
    private final List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
    private final Rect mScratchRect = new Rect();
    private final int[] mScratchCoords = new int[2];
    private Callback mCallback;
    private int mDismissAnimationRefCount = 0;
    private int mDragPosition = -1;

    public SwipeDismissListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final float densityScale = getResources().getDisplayMetrics().density;
        final float touchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(SwipeHelper.X, this, densityScale, touchSlop);

        mAnimationTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        final float pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }

    void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        final int childCount = getChildCount();
        getLocationOnScreen(mScratchCoords);
        final int x = (int) ev.getRawX() - mScratchCoords[0];
        final int y = (int) ev.getRawY() - mScratchCoords[1];
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            child.getHitRect(mScratchRect);
            if (mScratchRect.contains(x, y)) {
                return child;
            }
        }
        return null;
    }

    @Override
    public View getChildContentView(View v) {
        return v;
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        if (mDismissAnimationRefCount > 0) { // prevent until all current anims have finished
            return false;
        }
        if (v.getParent() == null) {
            // if there is no parent something is wrong, don't allow dismiss
            return false;
        }
        final int pos = getPositionForView(v);
        mDragPosition = pos;
        return mCallback.canDismiss(pos);
    }

    @Override
    public void onBeginDrag(View v) {
        // We need to prevent the surrounding ListView from intercepting us now;
        // the scroll position will be locked while we swipe
        requestDisallowInterceptTouchEvent(true);
    }

    @Override
    public void onChildDismissStart(View view) {
        mDismissAnimationRefCount++;
    }

    @Override
    public void onChildDismissed(final View dismissView) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        final ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --mDismissAnimationRefCount;
                if (mDismissAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    Collections.sort(mPendingDismisses);

                    final int[] dismissPositions = new int[mPendingDismisses.size()];
                    for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                        dismissPositions[i] = mPendingDismisses.get(i).position;
                    }
                    mCallback.onDismiss(SwipeDismissListView.this, dismissPositions);

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismiss : mPendingDismisses) {
                        // Reset view presentation
                        pendingDismiss.view.setAlpha(1f);
                        pendingDismiss.view.setTranslationX(0);
                        lp = pendingDismiss.view.getLayoutParams();
                        lp.height = pendingDismiss.originalHeight;
                        pendingDismiss.view.setLayoutParams(lp);
                    }

                    mPendingDismisses.clear();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                dismissView.setLayoutParams(lp);
            }
        });

        mPendingDismisses.add(new PendingDismissData(mDragPosition, dismissView, originalHeight));
        animator.start();

        mDragPosition = -1;
    }

    @Override
    public void onDragCancelled(View v) {
        mDragPosition = -1;
    }

    interface Callback {
        boolean canDismiss(int position);

        void onDismiss(SwipeDismissListView swipeDismissListView, int[] reverseSortedPositions);
    }

    private static class PendingDismissData implements Comparable<PendingDismissData> {
        final int position;
        final View view;
        final int originalHeight;

        public PendingDismissData(int position, View view, int originalHeight) {
            this.position = position;
            this.view = view;
            this.originalHeight = originalHeight;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }
}
