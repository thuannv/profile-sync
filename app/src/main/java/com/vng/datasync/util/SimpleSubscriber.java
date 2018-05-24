package com.vng.datasync.util;

import android.util.Log;

import rx.Subscriber;

/**
 * @author thuannv
 * @since 19/07/2017
 */
public class SimpleSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        Log.e("SimpleSubscriber", String.format("Error: %s", e.toString()));
    }

    @Override
    public void onNext(T t) {
    }
}
