package com.vng.datasync.data.remote.websocket;

import com.vng.datasync.data.remote.ChatHandler;

/**
 * @author thuannv
 * @since 25/05/2018
 */
public interface WebSocketManagerInf {
    void connect();
    void disconnect(String reason);
    boolean isConnected();
    void send(byte[] data);
    void setChatHandler(ChatHandler handler);
}
