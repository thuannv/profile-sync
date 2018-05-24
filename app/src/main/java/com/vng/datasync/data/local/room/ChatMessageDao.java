package com.vng.datasync.data.local.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.vng.datasync.data.model.roomdb.ChatMessageDBO;

import java.util.List;


/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 23/04/2018
 */

@Dao
public interface ChatMessageDao {

    @Query("SELECT * " +
            "FROM messages " +
            "WHERE room_id = :roomId")
    LiveData<List<ChatMessageDBO>> getMessageInRoom(long roomId);

    @Query("DELETE " +
            "FROM messages " +
            "WHERE room_id = :roomId")
    void deleteMessage(long roomId);

    @Query("UPDATE messages " +
            "SET unread = 0 " +
            "WHERE room_id = :roomId AND unread = 1")
    void markMessageRead(long roomId);

    @Query("SELECT * " +
            "FROM messages " +
            "WHERE message_id = :messageId AND channel_id = :channelId")
    ChatMessageDBO findMessageBy(int messageId, int channelId);

    @Query("SELECT * " +
            "FROM messages " +
            "WHERE id = :id")
    ChatMessageDBO findMessageByDBOId(long id);

    @Insert
    long insertMessage(ChatMessageDBO message);

    @Update
    void updateMessage(ChatMessageDBO message);
}
