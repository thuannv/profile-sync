package com.vng.datasync.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.vng.datasync.BuildConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 19/10/2017
 */

public class DSApplicationLifeCycle implements Application.ActivityLifecycleCallbacks {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(DSApplicationLifeCycle.class, BuildConfig.DEBUG && DEBUG);

    private static AtomicInteger mForegroundActivitiesCount;

    private static AtomicInteger mAliveActivitiesCount;

    public DSApplicationLifeCycle() {
        mForegroundActivitiesCount = new AtomicInteger(0);
        mAliveActivitiesCount = new AtomicInteger(0);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mAliveActivitiesCount.incrementAndGet();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mForegroundActivitiesCount.incrementAndGet();
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mForegroundActivitiesCount.decrementAndGet();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mAliveActivitiesCount.decrementAndGet();
        NotificationHelper.cancelWatchingStreamNotification();
    }

    public static boolean isBackground() {
        return mForegroundActivitiesCount.get() <= 0;
    }

    public static boolean isAppAlive() {
        return mAliveActivitiesCount.get() > 0;
    }
}
