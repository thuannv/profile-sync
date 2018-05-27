package com.vng.datasync.data.remote;

import com.google.protobuf.ByteString;
import com.vng.datasync.data.local.room.MessageIdGenerator;
import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.Constants;
import com.vng.datasync.util.AndroidUtilities;

import java.util.UUID;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 21/09/2017
 */

public final class MessageHelper {

    private static final String DEVICE_NAME = AndroidUtilities.getDeviceName();

    private static final String APP_VERSION = BuildConfig.VERSION_NAME;

    private static final String DEVICE_ID = AndroidUtilities.getDeviceId();

    private MessageHelper() {
    }

    public static ZLive.ZAPIPrivateChatItem createChatItem(int ownerId, String message, int contactId, long predictCreatedTime, int channelType) {
        return ZLive.ZAPIPrivateChatItem.newBuilder()
                .setOwnerId(ownerId)
                .setMessage(message)
                .setReceiverId(contactId)
                .setCreatedTime(predictCreatedTime)
                .setChannelType(channelType)
                .build();
    }

    public static ZLive.ZAPIPrivateChatItem createFakeChatItem(int ownerId, String message, int contactId, long predictCreatedTime, int channelType) {
        MessageIdGenerator instance = MessageIdGenerator.getInstance();
        int messageId = instance.generateId();
        return ZLive.ZAPIPrivateChatItem.newBuilder()
                .setMessageId(messageId)
                .setOwnerId(ownerId)
                .setMessage(message)
                .setReceiverId(contactId)
                .setCreatedTime(predictCreatedTime)
                .setChannelType(channelType)
                .build();
    }

    public static ZLive.ZAPIPrivateChatChannelMetaData createConfirmSyncedChannelData(int syncedOwnerId) {
        return ZLive.ZAPIPrivateChatChannelMetaData.newBuilder()
                .setOwnerId(syncedOwnerId)
                .build();
    }

    public static byte[] createMessage(int commandId, ByteString data, int requestId) {
        return createBaseBuilder(commandId, data)
                .setRequestId(requestId)
                .build()
                .toByteArray();
    }

    public static byte[] createMessage(int commandId, ByteString data) {
        return createBaseBuilder(commandId, data)
                .build()
                .toByteArray();
    }

    public static byte[] createMessage(int commandId, int subCommandId, ByteString data) {
        return createBaseBuilder(commandId, data)
                .setSubCmd(subCommandId)
                .build()
                .toByteArray();
    }

    public static ZLive.ZAPIMessage.Builder createBaseBuilder(int commandId, ByteString data) {
        return ZLive.ZAPIMessage.newBuilder()
                .setCmd(commandId)
                .setData(data)
                .setDeviceName(DEVICE_NAME)
                .setDeviceId(DEVICE_ID)
                .setAppVersion(APP_VERSION)
                .setPlatform(Constants.ANDROID_PLATFORM)
                .setTimestamp(System.currentTimeMillis());
    }
}
