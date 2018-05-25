package com.vng.datasync.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.BuildConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 25/09/2017
 */

public final class RequestHelper {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(RequestHelper.class, BuildConfig.DEBUG && DEBUG);

    private final AtomicInteger mAutoIncrementId = new AtomicInteger(0);

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Long> mRequests = Collections.synchronizedMap(new HashMap<>());

    private static volatile RequestHelper sInstance = null;

    private Handler mWorker;

    private RequestHelper() {
        HandlerThread t = new HandlerThread("private_chat_worker_thread");
        t.start();
        mWorker = new Handler(t.getLooper());
    }

    public static RequestHelper getInstance() {
        RequestHelper instance = sInstance;
        if (instance == null) {
            synchronized (RequestHelper.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new RequestHelper();
                }
            }
        }
        return instance;
    }

    public int getNextRequestId() {
        return mAutoIncrementId.incrementAndGet();
    }

    public long getMessageIdOfRequestId(int requestId) {
        Long messageId = mRequests.get(requestId);
        return messageId == null ? 0 : messageId;
    }

    private void add(int requestId, long messageId) {
        mRequests.put(requestId, messageId);
    }

    public boolean contains(int requestId) {
        return mRequests.containsKey(requestId);
    }

    public void remove(int requestId) {
        mRequests.remove(requestId);
    }

    public void addRequest(int requestId, long messageId, ZLive.ZAPIPrivateChatItem chatItem, long timeout, OnRequestTimeOut callback) {
        add(requestId, messageId);
        post(new RequestTimeOut(requestId, chatItem, callback), timeout);
    }

    private void post(Runnable task, long delay) {
        if (task != null) {
            mWorker.postDelayed(task, delay);
        }
    }

    /**
     * {@link RequestTimeOut}
     * Class is responsible for removing request which is timeout and notifying request was timeout
     * to whom it may concern in order to properly handle the request logic.
     */
    public final static class RequestTimeOut implements Runnable {

        private final int mRequestId;

        private final ZLive.ZAPIPrivateChatItem mChatItem;

        private final OnRequestTimeOut mCallback;

        RequestTimeOut(int requestId, ZLive.ZAPIPrivateChatItem chatItem, OnRequestTimeOut callback) {
            if (requestId <= 0) {
                throw new IllegalArgumentException("requestId must be positive.");
            }
            mRequestId = requestId;
            mChatItem = chatItem;
            mCallback = callback;
        }

        @Override
        public void run() {
            RequestHelper helper = RequestHelper.getInstance();
            if (helper.contains(mRequestId)) {
                if (mCallback != null) {
                    mCallback.onTimeOut(mRequestId, mChatItem);
                }
                helper.remove(mRequestId);
            }
        }
    }

    /**
     * {@link OnRequestTimeOut}
     * Callback for listening on a particular request if it was timeout.
     */
    public interface OnRequestTimeOut {
        void onTimeOut(int requestId, ZLive.ZAPIPrivateChatItem chatItem);
    }

}
