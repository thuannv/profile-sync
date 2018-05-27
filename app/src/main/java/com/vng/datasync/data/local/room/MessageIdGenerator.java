package com.vng.datasync.data.local.room;

import com.vng.datasync.Injector;
import com.vng.datasync.util.SimpleSubscriber;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import rx.schedulers.Schedulers;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 25/05/2018
 */

public final class MessageIdGenerator {
    private static MessageIdGenerator sInstance = null;

    private final AtomicInteger mId = new AtomicInteger();

    private final RoomDatabaseManager mDatabaseManager;

    private final CountDownLatch mCountDownLatch = new CountDownLatch(1);

    private volatile Initializer mInitializer;

    private MessageIdGenerator() {
        mDatabaseManager = Injector.providesDatabaseManager();
    }

    public static MessageIdGenerator getInstance() {
        return Holder.INSTANCE;
    }

    public void init() {
        if (mInitializer == null) {
            mInitializer = new Initializer(this);
            mInitializer.init();
        }
    }

    private void asyncInit() {
        mDatabaseManager.getLargestMessageId()
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleSubscriber<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        mId.set(integer);
                        mCountDownLatch.countDown();
                    }
                });
    }

    public int generateId() {
        try {
            mCountDownLatch.await();
            return mId.incrementAndGet();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * {@link Holder}
     */
    private static class Holder {
        private static final MessageIdGenerator INSTANCE = new MessageIdGenerator();
    }

    /**
     * {@link Initializer}
     */
    private static class Initializer {
        private WeakReference<MessageIdGenerator> mRef;

        private Initializer(MessageIdGenerator messageIdGenerator) {
            mRef = new WeakReference<>(messageIdGenerator);
        }

        private void init() {
            MessageIdGenerator messageIdGenerator = mRef.get();
            if (messageIdGenerator != null) {
                messageIdGenerator.asyncInit();
            }
        }
    }
}
