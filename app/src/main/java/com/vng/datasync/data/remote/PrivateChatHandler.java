package com.vng.datasync.data.remote;

import android.content.Context;
import android.os.Handler;

import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.Constants;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.event.Event;
import com.vng.datasync.data.event.EventDispatcher;
import com.vng.datasync.data.local.room.RoomDatabaseManager;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.model.roomdb.ChatMessageDBO;
import com.vng.datasync.data.remote.websocket.WebSocketManager;
import com.vng.datasync.util.CollectionUtils;
import com.vng.datasync.util.Logger;
import com.vng.datasync.util.NotificationHelper;
import com.vng.datasync.util.RequestHelper;
import com.vng.datasync.util.ProfileManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 25/09/2017
 */

public class PrivateChatHandler {

    private static volatile boolean sIsSyncingMessages = false;

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(PrivateChatHandler.class, BuildConfig.DEBUG && DEBUG);

    private static volatile long sCurrentContactId = 0;

    private final Handler mWatchDogsHandler = new Handler();

    private final ExecutorService mWorker;

    private final RoomDatabaseManager mRoomDatabaseManager;

    private final RequestHelper mRequestHelper;

    private final ProfileManager mProfileManager;

    private AtomicInteger mJobCount;

    private long mStartSyncTime;

    private final Runnable mWatchDogsRunnable = new Runnable() {
        @Override
        public void run() {
            L.e("Watch dog timeout");
            if (sIsSyncingMessages
                    && System.currentTimeMillis() - mStartSyncTime >= 5 * 60 * 1000) {
                mJobCount.set(0);
                sIsSyncingMessages = false;
                stopWatchingDogs();
            }
        }
    };

    private void startWatchingDogs() {
        try {
            mStartSyncTime = System.currentTimeMillis();
            mWatchDogsHandler.postDelayed(mWatchDogsRunnable, 5 * 60 * 1000);
        } catch (Exception e) {
            L.e(e.toString());
            mStartSyncTime = 0;
        }
    }

    private void stopWatchingDogs() {
        try {
            mWatchDogsHandler.removeCallbacks(mWatchDogsRunnable);
        } catch (Exception e) {
            L.e(e.toString());
        }
        mStartSyncTime = 0;
    }

    public PrivateChatHandler(@Nonnull Context context) {
        mWorker = Executors.newSingleThreadExecutor();
        mRoomDatabaseManager = RoomDatabaseManager.getInstance();
        mRequestHelper = RequestHelper.getInstance();
        mProfileManager = ProfileManager.getInstance();

        mProfileManager.init(context);
        mRoomDatabaseManager.initForUser(context);
    }

    public void handlePrivateChatMessage(final int subCommandId,
                                         final int requestId,
                                         final ZLive.ZAPIPrivateChatItem chatItem) {

        if (chatItem == null) {
            L.e("Chat item = null");

            return;
        }

//        L.e(format("Handle private chat.\nrequestId=%d\nownerId=%d\nreceiverId=%d",
//                requestId, chatItem.getOwnerId(), chatItem.getReceiverId()));

        if (isSentChatError(subCommandId)) {
            L.e("Send private chat message error");

//            EventDispatcher.getInstance().post(Event.SEND_CHAT_RESULT_EVENT, ChatMessage.STATE_SEND_ERROR, requestId, chatItem);

            updateMessageState(requestId, ChatMessage.STATE_SEND_ERROR);

            return;
        }

        final int unreadFlag;

        if (isSentChatSuccess(subCommandId)) {
//            EventDispatcher.getInstance().post(Event.SEND_CHAT_RESULT_EVENT, ChatMessage.STATE_SEND_SUCCESS, requestId, chatItem);

            updateMessageState(requestId, ChatMessage.STATE_SEND_SUCCESS);

            mRequestHelper.remove(requestId);

            return;
        } else {
            unreadFlag = 1;
        }

        doBackgroundWorks(subCommandId, chatItem, unreadFlag);
    }

    private void doBackgroundWorks(final int subCommandId, final ZLive.ZAPIPrivateChatItem chatItem, final int unreadFlag) {
        mWorker.execute(() -> {
            final boolean isReceiving = isReceiving(subCommandId);
            final int contactId = isReceiving ? chatItem.getOwnerId() : chatItem.getReceiverId();
            ChatMessage savedMessage = mRoomDatabaseManager.insertChatMessage(chatItem, contactId, unreadFlag);

            syncProfileIfNeeded(contactId);

            if (savedMessage == null) {
                L.e("Cannot save message.");
                return;
            }

            if (isReceiving) {
                confirmReceivedMessage(chatItem);

                EventDispatcher.getInstance().post(Event.NEW_PRIVATE_CHAT_MESSAGE_EVENT, savedMessage);

                if (!ChatConversation.isStrangerConversation(chatItem.getChannelType()) && !isChannelVisible(savedMessage.getFriendId())) {
                    createNotification(savedMessage);
                }
            }
        });
    }

    public void handleBlockedChat(int requestId) {
        L.e("Blocked private chat");

        EventDispatcher.getInstance().post(Event.BLOCKED_PRIVATE_CHAT_EVENT);

        mRequestHelper.remove(requestId);
    }

    private void createNotification(ChatMessage chatMessage) {
        Profile profile = ProfileManager.getInstance().get(chatMessage.getSenderId());

        NotificationHelper.createPrivateChatNotification(profile.getAvatar(),
                profile.getDisplayName(),
                chatMessage.getMessage(),
                chatMessage);
    }

    private void confirmReceivedMessage(ZLive.ZAPIPrivateChatItem chatItem) {
        byte[] message = MessageHelper.createMessage(Commands.CMD_CHAT_PRIVATE_RECEIVED_CONFIRM, chatItem.toByteString());
        WebSocketManager.getInstance().send(message);
    }

    private void syncProfileIfNeeded(int contactId) {
        Profile profile = mProfileManager.get(contactId);

        if (profile.isEmpty()) {
            mProfileManager.sync(contactId);
        }
    }

    private boolean isReceiving(int subcommand) {
        return Commands.SUB_CMD_NOTIFY_RECEIVED_PRIVATE_CHAT == subcommand;
    }

    private boolean isSentChatError(int subcommand) {
        return Commands.SUB_CMD_NOTIFY_SEND_CHAT_ERROR == subcommand;
    }

    private boolean isSentChatSuccess(int subCommand) {
        return Commands.SUB_CMD_NOTIFY_SEND_CHAT_SUCCESS == subCommand;
    }

    public static boolean isSyncingMessages() {
        return sIsSyncingMessages;
    }

    public boolean contains(int requestId) {
        return mRequestHelper.contains(requestId);
    }

    public void handleOfflineChannels(final List<ZLive.ZAPIPrivateChatChannelMetaData> channelsList) {
        if (CollectionUtils.isEmpty(channelsList)) {
            L.e("Offline messages-Null channel list");
            return;
        }

        if (sIsSyncingMessages) {
            return;
        }
        sIsSyncingMessages = true;

        mJobCount = new AtomicInteger(channelsList.size());

        mWorker.execute(() -> {
            WebSocketManager wsConnectionManager = WebSocketManager.getInstance();

            if (!wsConnectionManager.isConnected()) {
                sIsSyncingMessages = false;
                return;
            }

            startWatchingDogs();

            byte[] message = null;
            for (ZLive.ZAPIPrivateChatChannelMetaData channel : channelsList) {
                message = MessageHelper.createMessage(Commands.CMD_CHAT_PRIVATE, channel.toByteString());
                try {
                    wsConnectionManager.send(message);
                } catch (Exception e) {
                    L.e(e.toString());
                }
            }
        });
    }

    public void handleOfflineMessages(List<ZLive.ZAPIPrivateChatItem> chatItemsList) {
        mWorker.execute(new OfflineMessagesSyncJob(RoomDatabaseManager.getInstance(),
                mProfileManager,
                chatItemsList,
                this::syncCompleted));
    }

    private void syncCompleted(int syncedOwnerId) {
        if (syncedOwnerId > 0) {
            final WebSocketManager wsConnectionManager = WebSocketManager.getInstance();

            final ZLive.ZAPIPrivateChatChannelMetaData confirmedData = MessageHelper.createConfirmSyncedChannelData(syncedOwnerId);

            byte[] message = MessageHelper.createMessage(Commands.CMD_CHAT_PRIVATE_OFFLINE_RECEIVED_CONFIRM, confirmedData.toByteString());

            if (wsConnectionManager.isConnected()) {
                wsConnectionManager.send(message);
            }
        }

        if (mJobCount.decrementAndGet() <= 0) {
            sIsSyncingMessages = false;
            stopWatchingDogs();
            EventDispatcher.getInstance().post(Event.SYNC_OFFLINE_MESSAGES_COMPLETED);
        }
    }

    private void updateMessageState(int requestId, int state) {
        long messageId = mRequestHelper.getMessageIdOfRequestId(requestId);
        ChatMessageDBO dbo = mRoomDatabaseManager.findMessageById(messageId);
        dbo.state = state;
        mRoomDatabaseManager.updateMessage(dbo);
    }

    public static boolean isChannelVisible(long contactId) {
        return contactId == sCurrentContactId;
    }

    public static void setCurrentVisibleChannel(long contactId) {
        sCurrentContactId = contactId;
    }

    public static boolean shouldSyncOfflineMessages(long lastSyncTimestamp) {
        return !isSyncingMessages()
                && (System.currentTimeMillis() - lastSyncTimestamp > Constants.OFFLINE_MSG_SYNC_INTERVAL)
                && WebSocketManager.getInstance().isConnected();
    }
}
