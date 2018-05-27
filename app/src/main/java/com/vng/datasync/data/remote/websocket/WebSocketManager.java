package com.vng.datasync.data.remote.websocket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.vng.datasync.data.remote.ChatHandler;
import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.DataSyncApp;
import com.vng.datasync.event.Event;
import com.vng.datasync.event.EventDispatcher;
import com.vng.datasync.data.local.User;
import com.vng.datasync.data.local.UserManager;
import com.vng.datasync.data.model.ConfigsData;
import com.vng.datasync.data.remote.DataListener;
import com.vng.datasync.util.AndroidUtilities;
import com.vng.datasync.util.Logger;
import com.vng.datasync.util.NetworkUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.vng.datasync.data.remote.websocket.WebSocketConnection.*;
import static com.vng.datasync.data.remote.websocket.WebSocketConnection.State.newConnectedState;
import static com.vng.datasync.data.remote.websocket.WebSocketConnection.State.newConnectingState;
import static com.vng.datasync.data.remote.websocket.WebSocketConnection.State.newDisconnectedState;
import static com.vng.datasync.data.remote.websocket.WebSocketConnection.State.newErrorState;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 10/08/2017
 */

public final class WebSocketManager implements ConnectionObserver, WebSocketManagerInf {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(WebSocketManager.class, BuildConfig.DEBUG && DEBUG);

    @SuppressLint("StaticFieldLeak")
    private static volatile WebSocketManager sInstance = null;

    private final Context mContext;

    private volatile WebSocketConfigs mWebSocketConfigs;

    private WebSocketConnection mConnection;

    private final WebSocketCompositeListener mWebSocketCompositeListener;

    private final Handler mHandler = new Handler();

    private int mPingCount = 0;

    private int mRetryCount = 0;

    private State mConnectionState = newDisconnectedState("NEWLY_CREATED");

    private final Runnable mPingTask = new Runnable() {
        @Override
        public void run() {
            if (++mPingCount <= getMaxPingCount()) {
                ping();
                schedulePing();
            } else {
                L.e("*** WebSocket pings failed in %d times ***", mPingCount);
                toast(" WebSocket pings failed in " + mPingCount + " times");
                disconnect("PING_FAILED");
                mPingCount = 0;
            }
        }
    };

    private final Runnable mRetryRunnable = new Runnable() {
        @Override
        public void run() {
            if (++mRetryCount <= getMaxRetryCount()) {
                L.e("*** retry connect count=%d ***", mRetryCount);
                toast("WebSocket retry connect count=" + mRetryCount);
                disconnect("RETRY_CONNECT");
                internalConnect();
                mHandler.postDelayed(mRetryRunnable, getRetryTime());
            } else {
                toast("WebSocket retry connect failed " + mRetryCount + " times");
                L.e("*** WebSocket retry connect failed %d times ***", mRetryCount);
                disconnect("RETRY_CONNECT_FAILED");
                mRetryCount = 0;
            }
        }
    };

    private final Runnable mPingTimeoutChecker = new Runnable() {
        @Override
        public void run() {
            L.e("Ping timeout for connection checking -> dropped connection.");
            toast("ping timeout -> dropped connection");
            disconnect("PING_WAITING_TIMEOUT");
        }
    };

    public static WebSocketManager getInstance() {
        WebSocketManager localInstance = sInstance;
        if (localInstance == null) {
            synchronized (WebSocketManager.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    localInstance = sInstance = new WebSocketManager();
                }
            }
        }
        return localInstance;
    }

    private WebSocketManager() {
        mContext = DataSyncApp.getInstance().getApplicationContext();
        mWebSocketCompositeListener = new WebSocketCompositeListener();
    }

    private synchronized void setState(State newState) {
        mConnectionState = newState;
    }

    private synchronized boolean hasConfigs() {
        return mWebSocketConfigs != null;
    }

    private synchronized long getPingTime() {
        return hasConfigs() ? mWebSocketConfigs.getPingTime() : 60000;
    }

    private synchronized int getMaxRetryCount() {
        return hasConfigs() ? mWebSocketConfigs.getRetryCount() : 10;
    }

    private synchronized long getRetryTime() {
        return mRetryCount * (hasConfigs() ? mWebSocketConfigs.getRetryTime() : 10000);
    }

    private synchronized void schedulePing() {
        mHandler.postDelayed(mPingTask, getPingTime());
    }

    private synchronized void scheduleRetry() {
        if (isError() || isDisconnected()) {
            mHandler.postDelayed(mRetryRunnable, getRetryTime());
        }
    }

    private void stopRetry() {
        mHandler.removeCallbacks(mRetryRunnable);
    }

    private void stopPing() {
        mHandler.removeCallbacks(mPingTask);
    }


    private int getMaxPingCount() {
        return 3;
    }

    public synchronized boolean isConnecting() {
        return mConnectionState.isConnecting() || (mConnection != null && mConnection.isConnecting());
    }

    public synchronized boolean isConnected() {
        return mConnectionState.isConnected() && mConnection != null && mConnection.isConnected();
    }

    public synchronized boolean isDisconnected() {
        return mConnectionState.isDisconnected() || mConnection == null || mConnection.isDisconnected();
    }

    public synchronized boolean isError() {
        return mConnectionState.isError();
    }

    public synchronized String stateDescription() {
        return mConnectionState.toString();
    }

    private Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        return headers;
    }

    private synchronized void internalConnect() {
        try {
            URI wsUri = new URI(mWebSocketConfigs.wsUrl());
            if (mConnection != null) {
                mConnection.setConnectionObserver(null);
            }
            mConnection = new WebSocketConnection(wsUri, getHeaders(), 5000);
            mConnection.setConnectionObserver(this);
            mConnection.connect();
        } catch (Exception t) {
            L.e(t, "*** connect() -> connect failed error=%s -> retry connect after %d ms ***", t.toString(), getRetryTime());
            toast("WebSocket connect error=" + t.toString() + " -> retry connect after " + getRetryTime() + " ms");
            setState(newErrorState(t.getMessage()));
            if (mConnection != null) {
                mConnection.setConnectionObserver(null);
                mConnection = null;
            }
            scheduleRetry();
        }
    }

    public synchronized void connect() {
        if (!NetworkUtils.isNetworkConnected(mContext)) {
            L.e("*** no network connection ***");
            toast("WebSocket connect failed - N0 NETWORK CONNECTION");
            setState(newErrorState("NO_NETWORK_CONNECTION"));
            return;
        }

        if (isConnecting()) {
            L.e("*** WebSocket is connecting... ***");
            toast("WebSocket is connecting...");
            return;
        }

        if (isConnected()) {
            L.e("*** WebSocket is already connected ***");
            toast("WebSocket is already connected");
            return;
        }

        if (!init()) {
            L.e("*** WebSocket init failed ***");
            return;
        }

        setState(newConnectingState());

        internalConnect();
    }


    public synchronized void disconnect(String reason) {
        stopPing();
        stopRetry();
        stopCheckingPingTimeout();
        if (mConnection != null) {
            mConnection.setConnectionObserver(null);
            mConnection.disconnect();
            mConnection = null;
        }
        setState(newDisconnectedState(reason));
    }

    public synchronized void send(byte[] data) {
        if (isDisconnected()) {
            L.d("*** send() -> WebSocket is disconnected -> try to connect ***");
            toast("send() -> WebSocket is disconnected -> try to connect");
            connect();
        } else if (isError()) {
            L.d("*** send() -> WebSocket is error -> try to connect ***");
            toast("send() -> WebSocket is error -> try to connect");
            connect();
        } else if (isConnected()) {
            L.d("*** send() -> WebSocket is connected ***");
            //toast("send() -> WebSocket is connected -> sending message");
            mConnection.send(data);
        }
    }

    @Override
    public void setChatHandler(ChatHandler handler) {
        if (mWebSocketCompositeListener != null) {
            mWebSocketCompositeListener.setChatHandler(handler);
        }
    }

    private synchronized boolean init() {
        final User user = UserManager.getCurrentUser();
        if (user == null || TextUtils.isEmpty(user.getAccessToken())) {
            L.e("*** init(): failed! User or access token is EMPTY ***");
            toast("WebSocket init before user logged in");
            setState(newDisconnectedState("INIT_FAILED_NO_ACCESS_TOKEN"));
            return false;
        }

        final ConfigsData configs = ConfigsData.getDefaultConfigsData();
        mWebSocketConfigs = WebSocketConfigs.from(configs, user.getAccessToken());

        mRetryCount = 0;
        mPingCount = 0;
        return true;
    }

    private synchronized void ping() {
        if (mConnection != null) {
            mConnection.ping();
        }
    }

    public void addListener(DataListener listener) {
        if (listener != null) {
            mWebSocketCompositeListener.add(listener);
        }
    }

    public void removeListener(DataListener listener) {
        if (listener != null) {
            mWebSocketCompositeListener.remove(listener);
        }
    }

    @Override
    public synchronized void onConnected() {
        L.e("*** onConnected() -> stopRetry() ***");
        stopRetry();

        L.e("*** onConnected() -> setKeepAlive(true) ***");
        setKeepAlive(true);

        L.e("*** onConnected() -> start pinging and checking ping timeout ***");
        //ping();
        pingAndCheckTimeout();
    }

    private synchronized void pingAndCheckTimeout() {
        ping();
        checkPingTimeout();
    }

    private synchronized void stopCheckingPingTimeout() {
        mHandler.removeCallbacks(mPingTimeoutChecker);
    }

    private synchronized void checkPingTimeout() {
        // Start checking for receiving pong, the connection will be dropped after 30 secs we don't
        // receive corresponding pong.
        mHandler.postDelayed(mPingTimeoutChecker, 30000);
    }

    public synchronized void setKeepAlive(boolean enable) {
        if (mConnection != null) {
            mConnection.setKeepAlive(enable);
        }
    }

    @Override
    public void onReceived(byte[] data) {
        ZLive.ZAPIMessage message;
        try {
            message = ZLive.ZAPIMessage.parseFrom(data);
            mWebSocketCompositeListener.onMessage(message);
        } catch (Exception e) {
            L.e(e, "*** ERROR: %s ***", e.toString());
        }
    }

    @Override
    public synchronized void onError(String errorMessage) {
        errorMessage = TextUtils.isEmpty(errorMessage) ? "Unknown" : errorMessage;
        L.e("*** onError() -> error=%s -> will retry connect in %d ms ***", errorMessage, getRetryTime());
        toast(String.format(Locale.US, "WebSocket connect error=%s, will retry in %d ms", errorMessage, getRetryTime()));
        scheduleRetry();
    }

    @Override
    public synchronized void onDisconnected(int code, String reason, boolean remote) {
        L.e("*** onDisconnected(): code=%d, reason=%s, remote=%s ***", code, reason, remote);

        if (!TextUtils.isEmpty(reason) && reason.toLowerCase().contains("refuses handshake")) {
            toast("WebSocket handshake refuses");
            setState(newDisconnectedState("HANDSHAKE_REFUSES"));
            EventDispatcher.getInstance().post(Event.TOKEN_EXPIRED);
        } else {
            setState(newDisconnectedState(reason));
            L.e("*** onDisconnected(): will retry in %d ms ***", getRetryTime());
            toast("WebSocket disconnected, reason: " + reason + " will retry in " + getRetryTime() + " ms");
            scheduleRetry();
        }
    }

    @Override
    public synchronized void onPong() {
        if (isConnecting()) {
            // checking for the first pong when making connection
            L.e("*** onPong(): set WebSocket state connected ***");

            stopCheckingPingTimeout();

            setState(newConnectedState());

            schedulePing();

            EventDispatcher.getInstance().post(Event.WEBSOCKET_CONNECTED);
        } else {
            // reset ping count whenever receiving pong
            mPingCount = 0;
        }
    }

    private void toast(final String message) {
        if (DEBUG && BuildConfig.DEBUG) {
            AndroidUtilities.showToast(message);
        }
    }
}
