package com.vng.datasync.data.remote.websocket;

import com.vng.datasync.BuildConfig;
import com.vng.datasync.util.Logger;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.nio.ByteBuffer;

/**
 * @author thuannv
 * @since 23/05/2018
 */
public final class FakeWebSocketConnection {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(FakeWebSocketConnection.class, BuildConfig.DEBUG && DEBUG);

    private WebSocketConnection.ConnectionObserver mObserver;

    public void setConnectionObserver(WebSocketConnection.ConnectionObserver observer) {
        mObserver = observer;
    }

    public void onOpen(ServerHandshake handshakeData) {
        L.d("*** onOpen() ***");
        if (mObserver != null) {
            mObserver.onConnected();
        }
    }

    public void onMessage(String message) {
        L.d("*** onMessage(): message=%s ***", message + "");
    }

    public void onMessage(ByteBuffer bytes) {
        L.d("*** onMessage() ***");
        if (mObserver != null) {
            mObserver.onReceived(bytes.array());
        }
    }

    public void onClose(int code, String reason, boolean remote) {
        L.d("*** onClose(): code=%d, reason=%s ***", code, reason);
        if (mObserver != null) {
            mObserver.onDisconnected(code, reason, remote);
        }
    }

    public void onError(Exception ex) {
        final String error = ex.toString();
        L.d("*** onError(): error=%s ***", error);
        if (mObserver != null) {
            mObserver.onError(error);
        }
    }

    public void onWebsocketClosing(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
        L.d("*** onWebSocketClosing(): code=%d, reason=%s, remote=%s ***", code, reason, remote);
    }

    public void onWebsocketPong(org.java_websocket.WebSocket conn, Framedata f) {
        L.d("*** onWebsocketPong() ***");
        if (mObserver != null) {
            mObserver.onPong();
        }
    }

    public void onWebsocketPing(WebSocket conn, Framedata f) {
        L.d("*** onWebsocketPing() ***");
    }


}
