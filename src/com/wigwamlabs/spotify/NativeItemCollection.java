package com.wigwamlabs.spotify;

import java.util.ArrayList;

public abstract class NativeItemCollection<T extends NativeItem> extends NativeItem {
    private ArrayList<T> mItems;

    NativeItemCollection(int handle) {
        super(handle);
    }

    @Override
    public void destroy() {
        if (mItems != null) {
            for (T item : mItems) {
                if (item != null) {
                    item.destroy();
                }
            }
            mItems = null;
        }

        super.destroy();
    }

    void initList() {
        if (mItems == null) {
            final int count = nativeGetCount();
            mItems = new ArrayList<T>(count);
            for (int i = 0; i < count; i++) {
                mItems.add(null);
            }
        }
    }

    abstract int nativeGetCount();

    public int getCount() {
        initList();

        return mItems.size();
    }

    public T getItem(int index) {
        initList();

        // check cache
        T item = mItems.get(index);
        if (item != null) {
            return item;
        }

        // create new and cache
        item = createNewItem(index);
        mItems.set(index, item);
        return item;
    }

    abstract T createNewItem(int index);

    void onItemsMoved(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) {
            return;
        }

        if (mItems != null) {
            final T oldItem;
            if (oldPosition >= 0) { // move or remove
                oldItem = mItems.remove(oldPosition);
            } else { // new
                oldItem = null;
            }

            if (newPosition >= 0) { // move or add
                int pos = newPosition;
                if (newPosition > oldPosition && oldPosition >= 0) { // moving downwards
                    pos--; // the index is reported as too big
                }
                mItems.add(pos, oldItem);
            } else { // remove
                if (oldItem != null) {
                    oldItem.destroy();
                }
            }
        }
    }

    void onItemsMoved(int[] oldPositions, int newPosition) {
        if (mItems != null) {
            final int count = oldPositions.length;
            for (int i = 0; i < count; i++) {
                final int oldPosition = oldPositions[i];
                onItemsMoved(oldPosition, newPosition);

                final boolean added = (oldPosition < 0);
                final boolean moved = (oldPosition >= 0 && newPosition >= 0);
                final boolean movedLeft = (moved && newPosition < oldPosition);
                final boolean movedNowhere = (moved && newPosition == oldPosition);
                final boolean removed = (newPosition < 0);

                // adjust future source positions
                if (moved || removed) {
                    for (int j = i + 1; j < count; j++) {
                        final int futureOldPosition = oldPositions[j];
                        if (oldPosition < futureOldPosition && // item to the left
                                (removed || (newPosition > futureOldPosition))) { //removed or moved to right of future
                            oldPositions[j] = futureOldPosition - 1;
                        } else if (moved &&
                                (oldPosition > futureOldPosition && // item to the right of future
                                        (newPosition <= futureOldPosition))) {// moved to the left of future
                            oldPositions[j] = futureOldPosition + 1;
                        }
                    }
                }

                // adjust target position
                if (added || movedLeft || movedNowhere) {
                    newPosition++;
                }
            }
        }
    }

}
