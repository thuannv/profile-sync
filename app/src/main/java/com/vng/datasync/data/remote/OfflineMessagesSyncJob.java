package com.vng.datasync.data.remote;

import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.local.room.RoomDatabaseManager;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.util.CollectionUtils;
import com.vng.datasync.util.Logger;
import com.vng.datasync.util.NotificationHelper;
import com.vng.datasync.util.ProfileManager;

import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 02/10/2017
 */

public final class OfflineMessagesSyncJob implements Runnable {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(OfflineMessagesSyncJob.class, BuildConfig.DEBUG && DEBUG);

    private final RoomDatabaseManager mRoomDatabaseManager;

    private final ProfileManager mProfileManager;

    private final List<ZLive.ZAPIPrivateChatItem> mOfflineMessages;

    private final OfflineMessageSyncCallback mCallback;

    public OfflineMessagesSyncJob(RoomDatabaseManager databaseManager,
                                  ProfileManager profileManager,
                                  List<ZLive.ZAPIPrivateChatItem> offlineMessages,
                                  OfflineMessageSyncCallback callback) {
        mRoomDatabaseManager = databaseManager;
        mProfileManager = profileManager;
        mOfflineMessages = offlineMessages;
        mCallback = callback;
    }

    @Override
    public void run() {
        int ownerId = 0;

        if (!CollectionUtils.isEmpty(mOfflineMessages)) {
            final ZLive.ZAPIPrivateChatItem msg = mOfflineMessages.get(0);

            final int channelId = msg.getChannelId();

            ownerId = msg.getOwnerId();

            final int unread = ChatHandler.isChannelVisible(channelId) ? 0 : 1;

            ChatMessage savedMessage;

            for (ZLive.ZAPIPrivateChatItem chatItem : mOfflineMessages) {
                savedMessage = persistMessage(chatItem, chatItem.getOwnerId(), unread);

                if (savedMessage != null
                        && !ChatConversation.isStrangerConversation(chatItem.getChannelType())
                        && !ChatHandler.isChannelVisible(savedMessage.getChannelId())) {
                    createNotification(savedMessage);
                }
            }

            syncProfileIfNeeded(msg.getOwnerId());
        }

        if (mCallback != null) {
            mCallback.onSyncCompleted(ownerId);
        }
    }

    private void createNotification(ChatMessage chatMessage) {
        Profile profile = ProfileManager.getInstance().get(chatMessage.getSenderId());

        NotificationHelper.createPrivateChatNotification(profile.getAvatar(),
                profile.getDisplayName(),
                chatMessage.getMessage(),
                chatMessage);
    }

    private void syncProfileIfNeeded(int contactId) {
        Profile profile = mProfileManager.get(contactId);
        if (profile.isEmpty()) {
            mProfileManager.sync(contactId);
        }
    }

    private ChatMessage persistMessage(ZLive.ZAPIPrivateChatItem chat, int contactId, int unread) {
        return mRoomDatabaseManager.insertChatMessage(chat, contactId, unread);
    }

    /**
     * {@link OfflineMessageSyncCallback}
     */
    public interface OfflineMessageSyncCallback {
        void onSyncCompleted(int ownerId);
    }
}
