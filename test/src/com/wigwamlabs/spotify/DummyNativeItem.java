package com.wigwamlabs.spotify;

public class DummyNativeItem extends NativeItem {
    private final String mName;

    public DummyNativeItem(String name) {
        super(0);
        mName = name;
    }

    @Override
    void nativeDestroy() {
    }

    public String getName() {
        return mName;
    }
}
