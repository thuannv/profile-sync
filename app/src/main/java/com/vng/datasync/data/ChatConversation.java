package com.vng.datasync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.vng.datasync.protobuf.ZLive;

/**
 * @author thuannv
 * @since 08/02/2017
 */
public class ChatConversation implements Parcelable {

    private static final int TYPE_BASE = 0;
    public static final int TYPE_STRANGER = TYPE_BASE;
    public static final int TYPE_USER_CREATED = TYPE_BASE - 1;
    public static final int TYPE_FRIEND = TYPE_BASE + 1;
    public static final int TYPE_OFFICIAL = TYPE_BASE + 2;
    public static final int TYPE_CUSTOMER_SERVICE = TYPE_BASE + 3;

    private long mId;

    private int mChannelId;

    private int mContactId;

    private String mSnippet;

    private long mLastModifiedTime;

    private int mUnreadCount;

    private int mMessageCount;

    private int mConversationType;

    public ChatConversation() {}

    public ChatConversation(long conversationId, int channelId, int contactId, String snippet, long lastModifiedTime, int unreadCount, int messageCount, int conversationType) {
        mId = conversationId;
        mChannelId = channelId;
        mContactId = contactId;
        mSnippet = snippet;
        mLastModifiedTime = lastModifiedTime;
        mUnreadCount = unreadCount;
        mMessageCount = messageCount;
        mConversationType = conversationType;
    }

    public ChatConversation(int contactId, ZLive.ZAPIPrivateChatItem chatItem, int messageCount, int unreadCount) {
        mContactId = contactId;
        mChannelId = chatItem.getChannelId();
        mSnippet = chatItem.getMessage();
        mLastModifiedTime = chatItem.getCreatedTime();
        mConversationType = chatItem.getChannelType();
        mMessageCount = messageCount;
        mUnreadCount = unreadCount;
        mConversationType = chatItem.getChannelType();
    }

    protected ChatConversation(Parcel in) {
        mId = in.readLong();
        mChannelId = in.readInt();
        mContactId = in.readInt();
        mSnippet = in.readString();
        mLastModifiedTime = in.readLong();
        mUnreadCount = in.readInt();
        mMessageCount = in.readInt();
        mConversationType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeInt(mChannelId);
        dest.writeInt(mContactId);
        dest.writeString(mSnippet);
        dest.writeLong(mLastModifiedTime);
        dest.writeInt(mUnreadCount);
        dest.writeInt(mMessageCount);
        dest.writeInt(mConversationType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatConversation> CREATOR = new Creator<ChatConversation>() {
        @Override
        public ChatConversation createFromParcel(Parcel in) {
            return new ChatConversation(in);
        }

        @Override
        public ChatConversation[] newArray(int size) {
            return new ChatConversation[size];
        }
    };

    public String getSnippet() {
        return mSnippet;
    }

    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

    public int getContactId() {
        return mContactId;
    }

    public void setContactId(int mFriendId) {
        this.mContactId = mFriendId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getLastModifiedTime() {
        return mLastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        mLastModifiedTime = lastModifiedTime;
    }

    public int getUnreadCount() {
        return mUnreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        mUnreadCount = unreadCount;
    }

    public int getMessageCount() {
        return this.mMessageCount;
    }

    public void setMessageCount(int messageCount) {
        mMessageCount = messageCount;
    }

    public int getChannelId() {
        return mChannelId;
    }

    public void setChannelId(int channelId) {
        mChannelId = channelId;
    }

    public int getConversationType() {
        return mConversationType;
    }

    public void setConversationType(int conversationType) {
        mConversationType = conversationType;
    }

    public static boolean isStrangerConversation(int channelType) {
        return TYPE_STRANGER == channelType;
    }

    public static boolean isFriendConversation(int channelType) {
        return TYPE_FRIEND == channelType;
    }

    public static boolean isOfficialConversation(int channelType) {
        return TYPE_OFFICIAL == channelType;
    }

    public static boolean isUserCreatedConversation(int channelType) {
        return TYPE_USER_CREATED == channelType;
    }
}