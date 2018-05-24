package com.vng.datasync.util;

import rx.CompletableSubscriber;
import rx.Subscription;

/**
 * @author thuannv
 * @since 21/08/2017
 */
public class SimpleCompletableSubscriber implements CompletableSubscriber {

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
    }

    @Override
    public void onSubscribe(Subscription d) {
    }
}
