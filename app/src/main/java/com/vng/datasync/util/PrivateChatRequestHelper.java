package com.vng.datasync.util;

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

public class PrivateChatRequestHelper {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(PrivateChatRequestHelper.class, BuildConfig.DEBUG && DEBUG);

    private final AtomicInteger mAutoIncrementId = new AtomicInteger(0);

    private final Map<Integer, Long> mRequests = Collections.synchronizedMap(new HashMap<>());

    private static volatile PrivateChatRequestHelper sInstance = null;

    private Looper mLooper;

    private Handler mWorker;

    private PrivateChatRequestHelper() {
        HandlerThread t = new HandlerThread("private_chat_worker_thread");
        t.start();
        mLooper = t.getLooper();
        mWorker = new Handler(mLooper);
    }

    public static PrivateChatRequestHelper getInstance() {
        PrivateChatRequestHelper instance = sInstance;
        if (instance == null) {
            synchronized (PrivateChatRequestHelper.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new PrivateChatRequestHelper();
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

    public void add(int requestId, long messageId) {
        mRequests.put(requestId, messageId);
    }

    public boolean contains(int requestId) {
        return mRequests.containsKey(requestId);
    }

    public void remove(int requestId) {
        mRequests.remove(requestId);
    }

    public void addRequest(int requestId, long messageId, ZLive.ZAPIPrivateChatItem chatItem, long timeout, SendChatTimeOutCallback callback) {
        add(requestId, messageId);
        post(new ChatRequestTimeOut(requestId, chatItem, callback), timeout);
    }

    public void post(Runnable task, long delay) {
        if (task == null) {
            return;
        }

        if (delay <= 0) {
            mWorker.post(task);
        } else {
            mWorker.postDelayed(task, delay);
        }
    }

    public final static class ChatRequestTimeOut implements Runnable {

        private final int mRequestId;
        private final ZLive.ZAPIPrivateChatItem mChatItem;
        private final SendChatTimeOutCallback mCallback;

        public ChatRequestTimeOut(int requestId, ZLive.ZAPIPrivateChatItem chatItem, SendChatTimeOutCallback callback) {
            if (requestId <= 0) {
                throw new IllegalArgumentException("requestId must be positive.");
            }
            mRequestId = requestId;
            mChatItem = chatItem;
            mCallback = callback;
        }

        @Override
        public void run() {
            PrivateChatRequestHelper helper = PrivateChatRequestHelper.getInstance();
            if (helper.contains(mRequestId)) {
                if (mCallback != null) {
                    mCallback.onTimeOut(mRequestId, mChatItem);
                }
                helper.remove(mRequestId);
            }
        }
    }

    public interface SendChatTimeOutCallback {
        void onTimeOut(int requestId, ZLive.ZAPIPrivateChatItem chatItem);
    }

}
