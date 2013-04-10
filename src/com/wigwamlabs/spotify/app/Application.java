package com.wigwamlabs.spotify.app;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Debug.enableStrictMode();
    }
}
