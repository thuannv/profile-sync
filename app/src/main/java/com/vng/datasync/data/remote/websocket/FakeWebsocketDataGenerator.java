package com.vng.datasync.data.remote.websocket;

import com.google.gson.Gson;
import com.vng.datasync.DataSyncApp;
import com.vng.datasync.R;
import com.vng.datasync.data.DataProvider;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.remote.Commands;
import com.vng.datasync.data.remote.MessageHelper;
import com.vng.datasync.protobuf.ZLive;
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
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            L.d("Data generator is stopping...");
        }
    }

    private static final int[] mRandomStrings = {R.string.lorum_1, R.string.lorum_2, R.string.lorum_3, R.string.lorum_4};
    Gson mGson = new Gson();
    private void generate() {
        final Profile profile = mDataProvider.randomInRange();
        if (profile != null) {
            L.d("*** generate() -> profile = %s ***", mGson.toJson(profile));

            Random r = new Random();
            String chatMessage = DataSyncApp.getInstance().getString(mRandomStrings[r.nextInt(mRandomStrings.length)]);
            final ZLive.ZAPIPrivateChatItem chatItem = MessageHelper.createFakeChatItem(profile.getUserId(), chatMessage, 1, System.currentTimeMillis(), 0);
            final byte[] message = MessageHelper.createMessage(Commands.CMD_NOTIFY_STREAM, Commands.SUB_CMD_NOTIFY_RECEIVED_PRIVATE_CHAT, chatItem.toByteString());
            mWebSocketManager.onReceived(message);
        }
    }
}
