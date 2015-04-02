package com.gmail.mariska.fitfood.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Define a Service that returns an IBinder for the
 * sync adapter class, allowing the sync adapter framework to call
 * onPerformSync().
 */
public class FitFoodSyncService extends Service {
    private static final String LOG_TAG = FitFoodSyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static FitFoodSyncAdapter sFitFoodSyncAdapter = null;

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate - FitFoodSyncService");
        synchronized (sSyncAdapterLock) {
            if (sFitFoodSyncAdapter == null) {
                sFitFoodSyncAdapter = new FitFoodSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sFitFoodSyncAdapter.getSyncAdapterBinder();
    }
}