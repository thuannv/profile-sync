package com.vng.datasync.data.remote.websocket;

import com.vng.datasync.BuildConfig;
import com.vng.datasync.util.Logger;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 10/08/2017
 */

public class WebSocketConnection extends WebSocketClient {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(WebSocketConnection.class, BuildConfig.DEBUG && DEBUG);

    private ConnectionObserver mObserver;

    public WebSocketConnection(URI serverUri, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
    }

    public void setConnectionObserver(ConnectionObserver observer) {
        mObserver = observer;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        L.d("*** onOpen() ***");
        if (mObserver != null) {
            mObserver.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {
        L.d("*** onMessage(): message=%s ***", message + "");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        L.d("*** onMessage() ***");
        if (mObserver != null) {
            mObserver.onReceived(bytes.array());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        L.d("*** onClose(): code=%d, reason=%s ***", code, reason);
        if (mObserver != null) {
            mObserver.onDisconnected(code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        final String error = ex.toString();
        L.d("*** onError(): error=%s ***", error);
        if (mObserver != null) {
            mObserver.onError(error);
        }
    }

    @Override
    public void onWebsocketClosing(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
        L.d("*** onWebSocketClosing(): code=%d, reason=%s, remote=%s ***", code, reason, remote);
        super.onWebsocketClosing(conn, code, reason, remote);
    }

    @Override
    public void onWebsocketPong(org.java_websocket.WebSocket conn, Framedata f) {
        L.d("*** onWebsocketPong() ***");
        super.onWebsocketPong(conn, f);
        if (mObserver != null) {
            mObserver.onPong();
        }
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        L.d("*** onWebsocketPing() ***");
        super.onWebsocketPing(conn, f);
    }

    public boolean isConnected() {
        WebSocket webSocket = getConnection();
        return webSocket != null && webSocket.isOpen();
    }

    public boolean isDisconnected() {
        WebSocket webSocket = getConnection();
        return webSocket == null || webSocket.isClosed();
    }

    public boolean isClosing() {
        WebSocket webSocket = getConnection();
        return webSocket != null && webSocket.isClosing();
    }

    public boolean isFlushAndClose() {
        WebSocket webSocket = getConnection();
        return webSocket != null && webSocket.isFlushAndClose();
    }

    public Draft getDraft() {
        WebSocket webSocket = getConnection();
        if (webSocket != null) {
            return webSocket.getDraft();
        }
        return null;
    }

    public boolean isConnecting() {
        WebSocket webSocket = getConnection();
        return webSocket != null && webSocket.isConnecting();
    }

    public void send(byte[] data) {
        if (null == data || data.length == 0) {
            L.d("*** send(byte[] data) with empty data -> returns immediately ***");
            return;
        }

        if (isConnected()) {
            try {
                super.send(data);
            } catch (Exception e) {
                L.e(e, "*** send(byte[] data) -> WebSocket is not connected ***");
            }
        }
    }

    public void setKeepAlive(boolean enable) {
        try {
            getSocket().setKeepAlive(enable);
        } catch (Exception e) {
            L.e("*** setKeepAlive(%s) -> ERROR=%s ***", enable, e.toString());
        }
    }

    /**
     * connect to server
     */
    public void connect() {
        try {
            L.d("*** connect() ***");
            super.connect();
        } catch (IllegalStateException e) {
            onClose(CloseFrame.NEVER_CONNECTED, e.getMessage(), false);
        }
    }

    /**
     * schedulePingEvent to server check state life of client
     */
    public void ping() {
        try {
            WebSocket webSocket = getConnection();
            if (webSocket != null && webSocket.isOpen()) {
                PingFrame pingFrame = new PingFrame();
                pingFrame.setFin(true);
                webSocket.sendFrame(pingFrame);
            }
        } catch (Exception e) {
            L.e(e, "*** schedulePingEvent() -> failed. error=%s ***", e.toString());
        }
    }

    /**
     * call disconnect to server
     */
    public void disconnect() {
        L.d("*** disconnect() ***");
        if (!isDisconnected()) {
            L.d("*** disconnect() -> disconnecting to server ***");
            try {
                close();
                L.d("*** disconnect() -> disconnected ***");
            } catch (Exception e) {
                L.e(e, "*** disconnect() -> error=%s ***", e.toString());
            }
        }
    }


    /**
     * {@link ConnectionObserver}
     */
    public interface ConnectionObserver {

        void onConnected();

        void onReceived(byte[] data);

        void onError(String errorMessage);

        void onDisconnected(int code, String reason, boolean remote);

        void onPong();
    }

    /**
     * {@link State}
     */
    public static final class State {

        private static final int STATE_ERROR = -1;

        private static final int STATE_DISCONNECTED = STATE_ERROR + 1;

        private static final int STATE_CONNECTING = STATE_DISCONNECTED + 1;

        private static final int STATE_CONNECTED = STATE_CONNECTING + 1;

        public static State newConnectedState() {
            return new State(STATE_CONNECTED, "", "");
        }

        public static State newConnectingState() {
            return new State(STATE_CONNECTING, "", "");
        }

        public static State newDisconnectedState(String desc) {
            return new State(STATE_DISCONNECTED, "", desc);
        }

        public static State newErrorState(String error) {
            return new State(STATE_ERROR, error, "");
        }

        private final int mState;

        private final String mError;

        private final String mDisconnectedReason;

        private State(int state, String error, String disconnectedReason) {
            mState = state;
            mError = error;
            mDisconnectedReason = disconnectedReason;
        }

        public boolean isConnected() {
            return STATE_CONNECTED == mState;
        }

        public boolean isDisconnected() {
            return STATE_DISCONNECTED == mState;
        }

        public boolean isConnecting() {
            return STATE_CONNECTING == mState;
        }

        public boolean isError() {
            return STATE_ERROR == mState;
        }

        @Override
        public String toString() {
            if (isConnecting()) {
                return "state: CONNECTING";
            }
            if (isConnected()) {
                return "state: CONNECTED";
            }
            if (isDisconnected()) {
                return "state: DISCONNECTED - desc: " + mDisconnectedReason;
            }
            return "state: ERROR - error: " + mError;
        }
    }
}
