package com.vng.datasync.data;

import android.support.annotation.IntDef;

import com.vng.datasync.protobuf.ZLive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author thuannv
 * @since 18/08/2017
 */
public class ChatMessage {

    public static final int STATE_NEWLY_CREATE = 0;
    public static final int STATE_SEND_SUCCESS = STATE_NEWLY_CREATE + 1;
    public static final int STATE_SEND_ERROR = STATE_NEWLY_CREATE + 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ STATE_NEWLY_CREATE, STATE_SEND_SUCCESS, STATE_SEND_ERROR })
    public @interface ChatMessageState {}

    private long mId;

    private long mRoomId;

    private long mFriendId;

    private int mRequestId;

    private boolean mIsRead;

    private @ChatMessageState int mState;

    private ZLive.ZAPIPrivateChatItem mChatItem;

    public ChatMessage(ZLive.ZAPIPrivateChatItem chatItem) {
        mChatItem = chatItem;
        mState = STATE_NEWLY_CREATE;
    }

    public ChatMessage(long id, ZLive.ZAPIPrivateChatItem chatItem) {
        mId = id;
        mChatItem = chatItem;
        mState = STATE_SEND_SUCCESS;
    }

    public int getMessageId() {
        return mChatItem.getMessageId();
    }

    public int getMessageType() {
        return mChatItem.getMessageType();
    }

    public String getMessage() {
        return mChatItem.getMessage();
    }

    public String getJsonMessage() {
        return mChatItem.getJsonData();
    }

    public int getSenderId() {
        return mChatItem.getOwnerId();
    }

    public int getReceiverId() {
        return mChatItem.getReceiverId();
    }

    public long getAttachmentId() {
        return mChatItem.getAttachmentId();
    }

    public int getAttachmentType() {
        return mChatItem.getAttachmentType();
    }

    public long getCreatedTime() {
        return mChatItem.getCreatedTime();
    }

    public int getChannelId() {
        return mChatItem.getChannelId();
    }

    public long getId() {
        return mId;
    }

    public long getRoomId() {
        return mRoomId;
    }

    public int getChannelType() {
        return mChatItem.getChannelType();
    }

    public void setRoomId(long conversationId) {
        mRoomId = conversationId;
    }

    public int getRequestId() {
        return mRequestId;
    }

    public void setRequestId(int mRequestId) {
        this.mRequestId = mRequestId;
    }

    public ZLive.ZAPIPrivateChatItem getChatItem() {
        return mChatItem;
    }

    public void setChatItem(ZLive.ZAPIPrivateChatItem chatItem) {
        mChatItem = chatItem;
    }

    public @ChatMessageState int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public void setFriendId(long friendId) {
        mFriendId = friendId;
    }

    public long getFriendId() {
        return mFriendId;
    }

    public ChatMessage setId(long id) {
        mId = id;
        return this;
    }

    public static ChatMessage newMessage(ZLive.ZAPIPrivateChatItem chatItem) {
        return new ChatMessage(chatItem);
    }

    public boolean isRead() {
        return mIsRead;
    }

    public void setRead(boolean isRead) {
        mIsRead = isRead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;

        ChatMessage that = (ChatMessage) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}
