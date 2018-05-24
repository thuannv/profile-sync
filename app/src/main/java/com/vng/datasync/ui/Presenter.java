package com.vng.datasync.ui;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 14/08/2017
 */

public interface Presenter<V> {
    void attachView(V view);
    void detachView();
    boolean isViewAttached();
}
