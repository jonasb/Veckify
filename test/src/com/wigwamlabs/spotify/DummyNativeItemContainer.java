package com.wigwamlabs.spotify;

public class DummyNativeItemContainer extends NativeItemCollection<DummyNativeItem> {
    private String[] mNativeItems;

    public DummyNativeItemContainer() {
        super(1);
        mNativeItems = new String[]{};
    }

    @Override
    void nativeDestroy() {
    }

    @Override
    int nativeGetCount() {
        return mNativeItems.length;
    }

    @Override
    DummyNativeItem createNewItem(int index) {
        return new DummyNativeItem(mNativeItems[index]);
    }

    public String getContainerItems() {
        StringBuilder sb = new StringBuilder();
        final int count = getCount();
        for (int i = 0; i < count; i++) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            final DummyNativeItem item = getItem(i);
            sb.append(item.getName());
        }
        return sb.toString();
    }

    public String getNativeItems() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mNativeItems.length; i++) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(mNativeItems[i]);
        }
        return sb.toString();
    }

    public void setNativeItems(String[] nativeItems) {
        mNativeItems = nativeItems;
    }

    @Override
    public void onItemsMoved(int oldPosition, int newPosition) {
        super.onItemsMoved(oldPosition, newPosition);
    }

    @Override
    public void onItemsMoved(int[] oldPositions, int newPosition) {
        super.onItemsMoved(oldPositions, newPosition);
    }
}
