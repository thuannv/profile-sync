package com.vng.datasync.util;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 08/08/2017
 */

public final class RxUtils {

    private RxUtils() {}

    public static void unsubscribe(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public static void unsubscribe(CompositeSubscription subscriptions) {
        if (subscriptions != null) {
            subscriptions.clear();
        }
    }
}
