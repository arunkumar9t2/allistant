package com.arun.allistant;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Arun on 06/10/2016.
 */

public class Allistant extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
