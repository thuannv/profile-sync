package com.vng.datasync.data.remote.websocket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.data.local.UserManager;
import com.vng.datasync.data.remote.Commands;
import com.vng.datasync.data.remote.DataListener;
import com.vng.datasync.data.remote.MessageListener;
import com.vng.datasync.data.remote.PrivateChatHandler;
import com.vng.datasync.util.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    private final PrivateChatHandler mPrivateChatHandler;

    private final Set<DataListener> mListeners = Collections.synchronizedSet(new HashSet<DataListener>());

    private final DataListener mFirstListener = (commandId, subCommandId, data) -> {
        try {
            ZLive.ZAPIMessage message = (ZLive.ZAPIMessage) data;
            handle(commandId, subCommandId, message);
        } catch (Exception e) {
            L.e(e, "*** ERROR: %s ***", e.toString());
        }
    };

    public WebSocketCompositeListener(PrivateChatHandler privateChatHandler) {
        mPrivateChatHandler = privateChatHandler;
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
                handleOnlinePrivateChat(subCommandId, message);
            }
        } else if (commandId == Commands.CMD_CHAT_PRIVATE) {
            handleOfflineMessagesSyncing(subCommandId, message);
        }
    }

    private void handleOnlinePrivateChat(int subCommandId, ZLive.ZAPIMessage message) throws InvalidProtocolBufferException {
        ZLive.ZAPIPrivateChatItem chatItem = ZLive.ZAPIPrivateChatItem.parseFrom(message.getData());
        int requestId = message.getRequestId();
        int userId = UserManager.getCurrentUser().getUserId();

        switch (subCommandId) {
            case Commands.SUB_CMD_NOTIFY_RECEIVED_PRIVATE_CHAT:
                mPrivateChatHandler.handlePrivateChatMessage(subCommandId, requestId, chatItem);
                break;

            case Commands.SUB_CMD_NOTIFY_SUCCESS:
                if (mPrivateChatHandler.contains(requestId) && userId == chatItem.getOwnerId()) {
                    handleSendChatSuccess(subCommandId, requestId, chatItem);
                }
                break;

            case Commands.SUB_CMD_NOTIFY_BLOCKED_PRIVATE_CHAT:
                mPrivateChatHandler.handleBlockedChat(requestId);
                break;

            default:
                break;
        }
    }

    private void handleOfflineMessagesSyncing(int subCommandId, ZLive.ZAPIMessage message) throws InvalidProtocolBufferException {
        if (subCommandId == Commands.SUB_CMD_NOTIFY_PRIVATE_CHAT_UNREAD) {
            handleUnreadChannels(message);
        } else if (subCommandId == Commands.SUB_CMD_NOTIFY_PRIVATE_CHAT_UNREAD_RESPONSE) {
            handleUnreadMessages(message);
        }
    }

    private void handleUnreadChannels(ZLive.ZAPIMessage message) throws InvalidProtocolBufferException {
        ZLive.ZAPIPrivateChatUnread chatUnread = ZLive.ZAPIPrivateChatUnread.parseFrom(message.getData());

        List<ZLive.ZAPIPrivateChatChannelMetaData> channelsList = chatUnread.getChatChannelsList();

        mPrivateChatHandler.handleOfflineChannels(channelsList);
    }

    private void handleUnreadMessages(ZLive.ZAPIMessage message) throws InvalidProtocolBufferException {
        ZLive.ZAPIPrivateChatUnreadResponse chatUnreadResponse = ZLive.ZAPIPrivateChatUnreadResponse.parseFrom(message.getData());

        List<ZLive.ZAPIPrivateChatItem> chatItemsList = chatUnreadResponse.getChatItemsList();

        mPrivateChatHandler.handleOfflineMessages(chatItemsList);
    }

    private void handleSendChatSuccess(int subCommandId, int requestId, ZLive.ZAPIPrivateChatItem chatItem) {
        int newSubCommandId = Commands.SUB_CMD_NOTIFY_SEND_CHAT_SUCCESS;

        if (subCommandId == Commands.SUB_CMD_NOTIFY_ERROR) {
            newSubCommandId = Commands.SUB_CMD_NOTIFY_SEND_CHAT_ERROR;
        }

        mPrivateChatHandler.handlePrivateChatMessage(newSubCommandId, requestId, chatItem);
    }
}
