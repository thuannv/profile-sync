package com.vng.datasync.ui;

/**
 * @author thuannv
 * @since 18/07/2017
 */
public class BasePresenter<V> implements Presenter<V>{

    protected V mView;

    public void attachView(V view) {
        mView = view;
    }

    public void detachView() {
        mView = null;
    }

    public boolean isViewAttached() {
        return null != mView;
    }
}
