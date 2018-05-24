package com.vng.datasync.data.local.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.vng.datasync.data.model.roomdb.ChatConversationDBO;

import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 23/04/2018
 */

@Dao
public interface ChatConversationDao {

    @Query("SELECT * " +
            "FROM chat_rooms " +
            "ORDER BY modified_time DESC")
    LiveData<List<ChatConversationDBO>> getHomeConversations();

    @Query("SELECT * " +
            "FROM chat_rooms " +
            "WHERE friend_id = :friendId")
    ChatConversationDBO findConversationByFriendId(int friendId);

    @Query("SELECT * " +
            "FROM chat_rooms " +
            "WHERE conversation_type = :channelType")
    ChatConversationDBO findConversationByChannelType(int channelType);

    @Query("SELECT * " +
            "FROM chat_rooms " +
            "WHERE room_id = :id")
    ChatConversationDBO findConversationById(long id);

    @Query("SELECT message_count " +
            "FROM chat_rooms " +
            "WHERE conversation_type = :channelType")
    int getMessagesCountOfChannelType(int channelType);

    @Query("UPDATE chat_rooms "+
            "SET snippet = " +
                "(SELECT message " +
                 "FROM messages " +
                 "WHERE channel_id = :channelId ORDER BY created_time), " +
                "modified_time = " +
                "(SELECT created_time " +
                 "FROM messages " +
                 "WHERE channel_id = :channelId ORDER BY created_time) " +
            "WHERE channel_id = :channelId")
    void updateConversation(int channelId);

    @Insert
    long insertConversation(ChatConversationDBO dbo);

    @Update
    void updateConversation(ChatConversationDBO dbo);

    @Query("DELETE " +
            "FROM chat_rooms " +
            "WHERE room_id = :conversationId")
    void deleteConversation(long conversationId);

    @Delete
    void deleteConversation(ChatConversationDBO dbo);

    @Query("SELECT count(*) " +
            "FROM chat_rooms " +
            "WHERE unread_count > 0")
    int getUnreadConversationsCount();
}
