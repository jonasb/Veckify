package com.wigwamlabs.spotify;

import java.util.ArrayList;

public abstract class NativeItemCollection<T extends NativeItem> extends NativeItem {
    private ArrayList<T> mItems;

    public NativeItemCollection(int handle) {
        super(handle);
    }

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


    public void onItemsMoved(int oldPosition, int newPosition) {
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
}
