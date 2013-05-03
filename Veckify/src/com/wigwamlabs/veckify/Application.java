package com.wigwamlabs.veckify;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Debug.logLifecycle("Application.onCreate()");
        Debug.enableStrictMode();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Debug.logLifecycle("Application.onTerminate()");
    }
}
