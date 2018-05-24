package com.vng.datasync.data.remote.websocket;

import com.google.gson.Gson;
import com.vng.datasync.data.DataProvider;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.util.Logger;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * @author thuannv
 * @since 24/05/2018
 */
public final class FakeWebsocketDataGenerator {

    private static final Logger L = Logger.getLogger("WSDataGenerator", true);

    private FakeWebSocketManager mWebSocketManager;

    private DataGenerator mDataGenerator;

    private DataProvider mDataProvider;

    private FakeWebsocketDataGenerator() {
        mDataProvider = DataProvider.getInstance();
    }

    public static FakeWebsocketDataGenerator getInstance() {
        return Holder.INSTANCE;
    }

    public void setWebSocketManager(FakeWebSocketManager manager) {
        mWebSocketManager = manager;
    }

    public void startGenerator() {
        synchronized (this) {
            if (mDataGenerator == null) {
                mDataGenerator = new DataGenerator(this);
            }
        }
        if (mDataGenerator.isRunning()) {
            L.w("Generator is already started.");
            return ;
        }
        mDataGenerator.setRunning(true);
        new Thread(mDataGenerator).start();
    }

    public void shutdownGenerator() {
        synchronized (this) {
            if (mDataGenerator != null && mDataGenerator.isRunning()) {
                mDataGenerator.setRunning(false);
            }
        }
    }

    /**
     * {@link Holder}
     */
    private static class Holder {
        private static final FakeWebsocketDataGenerator INSTANCE = new FakeWebsocketDataGenerator();
    }

    /**
     * {@link DataGenerator}
     */
    private static class DataGenerator implements Runnable {

        private volatile boolean mIsRunning = false;

        private final WeakReference<FakeWebsocketDataGenerator> mGeneratorRef;

        DataGenerator(FakeWebsocketDataGenerator generator) {
            mGeneratorRef = new WeakReference<>(generator);
        }

        public void setRunning(boolean isRunning) {
            mIsRunning = isRunning;
        }

        public boolean isRunning() {
            return mIsRunning;
        }

        @Override
        public void run() {
            L.d("DataGenerator is running.");
            final Random random = new Random();
            final FakeWebsocketDataGenerator generator = mGeneratorRef.get();
            while (mIsRunning && generator != null) {
                generator.generate();
                try {
                    Thread.sleep(random.nextInt(200));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            L.d("Data generator is stopping...");
        }
    }

    Gson mGson = new Gson();
    private void generate() {
        final Profile profile = mDataProvider.random();
        if (profile != null) {
            L.d("*** generate() -> profile = %s ***", mGson.toJson(profile));
        }
    }
}
