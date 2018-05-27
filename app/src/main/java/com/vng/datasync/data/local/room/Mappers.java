package com.vng.datasync.data.local.room;

import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.Mapper;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.model.roomdb.ChatConversationDBO;
import com.vng.datasync.data.model.roomdb.ChatMessageDBO;
import com.vng.datasync.data.model.roomdb.ChatProfileDBO;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 26/04/2018
 */
public final class Mappers {
    public static final Mapper<ChatConversationDBO, ChatConversation> FROM_DBO_TO_CONVERSATION = dbo -> {
        ChatConversation chatConversation = new ChatConversation();

        if (dbo != null) {
            chatConversation.setId(dbo.roomId);
            chatConversation.setChannelId(dbo.channelId);
            chatConversation.setContactId(dbo.friendId);
            chatConversation.setSnippet(dbo.snippet);
            chatConversation.setLastModifiedTime(dbo.modifiedTime);
            chatConversation.setUnreadCount(dbo.unreadCount);
            chatConversation.setMessageCount(dbo.messageCount);
            chatConversation.setConversationType(dbo.conversationType);
        }

        return chatConversation;
    };

    public static final Mapper<ChatConversation, ChatConversationDBO> FROM_CONVERSATION_TO_DBO = conversation -> {
        ChatConversationDBO dbo = new ChatConversationDBO();

        if (conversation != null) {
            dbo.friendId = conversation.getContactId();
            dbo.snippet = conversation.getSnippet();
            dbo.modifiedTime = conversation.getLastModifiedTime();
            dbo.channelId = conversation.getChannelId();
            dbo.unreadCount = conversation.getUnreadCount();
            dbo.messageCount = conversation.getMessageCount();
            dbo.conversationType = conversation.getConversationType();
        }

        return dbo;
    };

    public static final Mapper<ChatMessageDBO, ChatMessage> FROM_DBO_TO_CHAT_MESSAGE = dbo -> {
        if (dbo != null) {
            ZLive.ZAPIPrivateChatItem chatItem = ZLive.ZAPIPrivateChatItem.newBuilder()
                    .setMessageId(dbo.messageId)
                    .setReceiverId(dbo.receiverId)
                    .setOwnerId(dbo.senderId)
                    .setMessage(dbo.message)
                    .setCreatedTime(dbo.createdTime)
                    .setMessageType(dbo.messageType)
                    .setAttachmentId(dbo.attachmentId)
                    .setAttachmentType(dbo.attachmentType)
                    .build();

            ChatMessage chatMessage = new ChatMessage(dbo.id, chatItem);
            chatMessage.setRead(dbo.unreadFlag == 0);
            chatMessage.setState(dbo.state == null ? 0 : dbo.state);

            return chatMessage;
        }

        return null;
    };

    public static final Mapper<ChatMessage, ChatMessageDBO> FROM_CHAT_MESSAGE_TO_DBO = chatMessage -> {
        ChatMessageDBO dbo = new ChatMessageDBO();

        if (chatMessage != null) {
            dbo.attachmentId = chatMessage.getAttachmentId();
            dbo.attachmentType = chatMessage.getAttachmentType();
            dbo.channelId = chatMessage.getChannelId();
            dbo.createdTime = chatMessage.getCreatedTime();
            dbo.message = chatMessage.getMessage();
            dbo.messageId = chatMessage.getMessageId();
            dbo.messageType = chatMessage.getMessageType();
            dbo.receiverId = chatMessage.getReceiverId();
            dbo.roomId = chatMessage.getRoomId();
            dbo.senderId = chatMessage.getSenderId();
            dbo.unreadFlag = chatMessage.isRead() ? 0 : 1;
            dbo.state = chatMessage.getState();
        }

        return dbo;
    };

    public static final Mapper<ChatProfileDBO, Profile> FROM_DBO_TO_PROFILE = dbo -> {
        Profile profile = new Profile();

        if (dbo != null) {
            profile.setUserId(dbo.userId);
            profile.setDisplayName(dbo.displayName);
            profile.setAvatar(dbo.avatar);
            profile.setState(dbo.state);
        }

        return profile;
    };

    public static final Mapper<Profile, ChatProfileDBO> FROM_PROFILE_TO_DBO = profile -> {
        ChatProfileDBO dbo = new ChatProfileDBO();

        if (profile != null) {
            dbo.userId = profile.getUserId();
            dbo.displayName = profile.getDisplayName();
            dbo.avatar = profile.getAvatar();
            dbo.state = profile.getState();
        }

        return dbo;
    };
}
