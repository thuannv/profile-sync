package com.vng.datasync.util;

import com.vng.datasync.BuildConfig;
import com.vng.datasync.data.remote.rest.response.Response;

/**
 * Copyright (C) 2017, VNG Corporation.
 * <p>
 * Created by Taindb
 * on 8/24/2017.
 */

public abstract class ResponseSubscriber<T extends Response> extends SimpleSubscriber<T> {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(ResponseSubscriber.class, BuildConfig.DEBUG && DEBUG);

    @Override
    public void onNext(T t) {
        if (t.getCode() == 0) {
            onSuccess(t);
        } else {
            onFailure(t);
        }
    }

    public void onFailure(T t) {
        L.e(t.getMessage());
    }

    public abstract void onSuccess(T t);
}
