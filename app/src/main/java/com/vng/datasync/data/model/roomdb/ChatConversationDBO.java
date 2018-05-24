package com.vng.datasync.data.model.roomdb;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 23/04/2018
 */

@Entity(tableName = "chat_rooms",
indices = {@Index(name = "index_conversation_friend_id", value = "friend_id")})
//@Entity(tableName = "chat_rooms")
public class ChatConversationDBO {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "room_id")
    public Long roomId;

    @ColumnInfo(name = "friend_id")
    public Integer friendId;

    @ColumnInfo(name = "snippet")
    public String snippet;

    @ColumnInfo(name = "created_time")
    public Long createdTime;

    @ColumnInfo(name = "modified_time")
    public Long modifiedTime;

    @ColumnInfo(name = "channel_id")
    public Integer channelId;

    @ColumnInfo(name = "unread_count")
    public Integer unreadCount;

    @ColumnInfo(name = "message_count")
    public Integer messageCount;

    @ColumnInfo(name = "conversation_type")
    public Integer conversationType;
}
