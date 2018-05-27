package com.vng.datasync.data.remote.websocket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.data.remote.Commands;
import com.vng.datasync.data.remote.DataListener;
import com.vng.datasync.data.remote.MessageListener;
import com.vng.datasync.data.remote.ChatHandler;
import com.vng.datasync.util.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 02/10/2017
 */

public final class WebSocketCompositeListener implements MessageListener {

    private static final boolean DEBUG = false;

    private static final Logger L = Logger.getLogger(WebSocketCompositeListener.class, BuildConfig.DEBUG && DEBUG);

    private ChatHandler mChatHandler;

    private final Set<DataListener> mListeners = Collections.synchronizedSet(new HashSet<DataListener>());

    private final DataListener mFirstListener = (commandId, subCommandId, data) -> {
        try {
            ZLive.ZAPIMessage message = (ZLive.ZAPIMessage) data;
            handle(commandId, subCommandId, message);
        } catch (Exception e) {
            L.e(e, "*** ERROR: %s ***", e.toString());
        }
    };

    public WebSocketCompositeListener() {
    }

    public void setChatHandler(ChatHandler handler) {
        mChatHandler = handler;
    }

    @Override
    public void onMessage(ZLive.ZAPIMessage message) {
        int cmd = message.getCmd();
        int subCmd = message.getSubCmd();

        mFirstListener.onReceive(cmd, subCmd, message);

        notifyOtherListeners(cmd, subCmd, message);
    }

    private void notifyOtherListeners(int cmd, int subCmd, ZLive.ZAPIMessage message) {
        for (DataListener listener : mListeners) {
            listener.onReceive(cmd, subCmd, message);
        }
    }

    public void add(DataListener listener) {
        mListeners.add(listener);
    }

    public void remove(DataListener listener) {
        mListeners.remove(listener);
    }

    private void handle(int commandId, int subCommandId, ZLive.ZAPIMessage message) throws InvalidProtocolBufferException {
        if (commandId == Commands.CMD_NOTIFY_STREAM) {
            if (subCommandId == Commands.SUB_CMD_NOTIFY_TOTAL_FRIEND_REQUEST) {
            } else {
                if (mChatHandler != null) {
                    mChatHandler.handleOnlinePrivateChat(subCommandId, message);
                }
            }
        } else if (commandId == Commands.CMD_CHAT_PRIVATE) {
            if (mChatHandler != null) {
                mChatHandler.handleOfflineMessagesSyncing(subCommandId, message);
            }
        }
    }


}
