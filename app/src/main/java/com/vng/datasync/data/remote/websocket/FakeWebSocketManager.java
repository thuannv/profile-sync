package com.vng.datasync.data.remote.websocket;

import android.content.Context;
import android.support.annotation.NonNull;

import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.DataSyncApp;
import com.vng.datasync.data.remote.DataListener;
import com.vng.datasync.data.remote.PrivateChatHandler;
import com.vng.datasync.util.Logger;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * @author thuannv
 * @since 23/05/2018
 */
public final class FakeWebSocketManager implements WebSocketConnection.ConnectionObserver, WebSocketManagerInf {

    private static final Logger L = Logger.getLogger("FakeWebSocketManager", true);

    private WebSocketCompositeListener mWebsocketListeners;

    private static volatile FakeWebSocketManager sInstance = null;

    private FakeWebSocketConnection mConnection;

    private final CountDownLatch mInitializedLock = new CountDownLatch(1);

    private Initializer mInitializer;

    private FakeWebSocketManager() {
        Context context = DataSyncApp.getInstance().getApplicationContext();
        mWebsocketListeners = new WebSocketCompositeListener(new PrivateChatHandler(context));
    }

    public static FakeWebSocketManager getInstance() {
        FakeWebSocketManager instance = sInstance;
        if (sInstance == null) {
            synchronized (FakeWebSocketManager.class) {
                instance = sInstance;
                if (sInstance == null) {
                    instance = sInstance = new FakeWebSocketManager();
                }
            }
        }
        return instance;
    }

    public void addListener(DataListener listener) {
        if (listener != null) {
            mWebsocketListeners.add(listener);
        }
    }

    public void removeListener(DataListener listener) {
        if (listener != null) {
            mWebsocketListeners.remove(listener);
        }
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onReceived(byte[] data) {
        ZLive.ZAPIMessage message;
        try {
            message = ZLive.ZAPIMessage.parseFrom(data);
            mWebsocketListeners.onMessage(message);
        } catch (Exception e) {
            L.e(e, "*** ERROR: %s ***", e.toString());
        }
    }

    @Override
    public void onError(String errorMessage) {
    }

    @Override
    public void onDisconnected(int code, String reason, boolean remote) {
    }

    @Override
    public void onPong() {
    }

    public void init() {
        L.d("*** init() ***");
        Initializer initializer = mInitializer;
        if (initializer == null) {
            synchronized (this) {
                initializer = mInitializer;
                if (initializer == null) {
                    initializer = mInitializer = new Initializer(this);
                    initializer.ayncInit(mInitializedLock);
                }
            }
        }
    }

    @Override
    public void connect() {
        //TODO:
    }

    @Override
    public void disconnect(String reason) {
        //TODO:
    }

    @Override
    public void send(byte[] data) {
        //TODO:
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    /**
     * {@link Initializer}
     * Helper class for asynchronously initialize {@link FakeWebSocketManager}
     */
    private static final class Initializer {

        private final WeakReference<FakeWebSocketManager> mManagerRef;

        public Initializer(FakeWebSocketManager manager) {
            mManagerRef = new WeakReference<>(manager);
        }

        public void ayncInit(@NonNull CountDownLatch initLock) {
            L.d("*** FakeWebSocketManager.Initializer.ayncInit() ***");
            new Thread(() -> {
                try {
                    FakeWebSocketManager manager = mManagerRef.get();
                    if (manager != null) {
                        manager.internalInitialize();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    initLock.countDown();
                }
            }).start();
        }
    }

    private void internalInitialize() {
        mConnection = new FakeWebSocketConnection();
        mConnection.setConnectionObserver(this);
    }
}
