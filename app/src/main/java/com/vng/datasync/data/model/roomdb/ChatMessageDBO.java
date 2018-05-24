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

@Entity(tableName = "messages",
        indices = {@Index(name = "index_message_id", value = "message_id"),
                @Index(name = "index_message_receiver_id", value = "receiver_id"),
                @Index(name = "index_message_sender_id", value = "sender_id")})
//@Entity(tableName = "messages")
public class ChatMessageDBO {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Long id;

    @ColumnInfo(name = "message_id")
    public Integer messageId;

    @ColumnInfo(name = "receiver_id")
    public Integer receiverId;

    @ColumnInfo(name = "sender_id")
    public Integer senderId;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "created_time")
    public Long createdTime;

    @ColumnInfo(name = "message_type")
    public Integer messageType;

    @ColumnInfo(name = "room_id")
    public Long roomId;

    @ColumnInfo(name = "attachment_id")
    public Long attachmentId;

    @ColumnInfo(name = "attachment_type")
    public Integer attachmentType;

    @ColumnInfo(name = "channel_id")
    public Integer channelId;

    @ColumnInfo(name = "unread")
    public Integer unreadFlag;

    @ColumnInfo(name = "state")
    public Integer state;
}
