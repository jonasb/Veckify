package com.wigwamlabs.veckify;

import com.wigwamlabs.veckify.db.DataDatabaseAdapter;

public class Application extends android.app.Application {
    private DataDatabaseAdapter mDb;

    @Override
    public void onCreate() {
        Debug.logLifecycle("Application.onCreate()");
        super.onCreate();
        Debug.enableStrictMode();

        mDb = new DataDatabaseAdapter(this);
    }

    @Override
    public void onTerminate() {
        Debug.logLifecycle("Application.onTerminate()");
        super.onTerminate();

        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    DataDatabaseAdapter getDb() {
        return mDb;
    }
}
