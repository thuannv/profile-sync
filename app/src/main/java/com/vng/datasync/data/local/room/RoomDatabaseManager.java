package com.vng.datasync.data.local.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.model.roomdb.ChatConversationDBO;
import com.vng.datasync.data.model.roomdb.ChatMessageDBO;
import com.vng.datasync.data.model.roomdb.ChatProfileDBO;

import java.util.List;

import rx.Completable;
import rx.Observable;

import static com.vng.datasync.data.local.room.Mappers.FROM_CHAT_MESSAGE_TO_DBO;
import static com.vng.datasync.data.local.room.Mappers.FROM_CONVERSATION_TO_DBO;
import static com.vng.datasync.data.local.room.Mappers.FROM_DBO_TO_CONVERSATION;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 26/04/2018
 */
public class RoomDatabaseManager {

    private static final String TAG = RoomDatabaseManager.class.getSimpleName();

    private UserDatabase mUserDatabase;

    private ChatMessageDao mChatMessageDao;

    private ChatConversationDao mChatConversationDao;

    private ChatProfileDao mChatProfileDao;

    private static final Migration mUserDBMigration_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE friend_requests(id INTEGER PRIMARY KEY AUTOINCREMENT, total_friend_requests INTEGER)");
        }
    };

    private static final RoomDatabase.Callback mUserDBCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            db.execSQL("CREATE INDEX index_conversation_friend_id ON chat_rooms (friend_id)");
//            db.execSQL("CREATE INDEX index_message_id ON messages (message_id)");
//            db.execSQL("CREATE INDEX index_message_receiver_id ON messages (receiver_id)");
//            db.execSQL("CREATE INDEX index_message_sender_id ON messages (sender_id)");
//            db.execSQL("CREATE INDEX index_user_profile_id ON chat_profiles (user_id)");
//            db.execSQL("CREATE INDEX index_block_user_profile_id ON block_profiles (profile_id)");
            db.execSQL("CREATE TRIGGER update_room_on_insert_message AFTER INSERT ON messages WHEN new.unread = 1 BEGIN UPDATE chat_rooms SET unread_count = unread_count + 1 WHERE room_id = new.room_id; END");
            db.execSQL("CREATE TRIGGER update_unread_count_on_update_message AFTER UPDATE OF unread ON messages WHEN new.unread = 0 BEGIN UPDATE chat_rooms SET unread_count = unread_count - 1 WHERE room_id = new.room_id; END");
            db.execSQL("CREATE TRIGGER update_message_count_on_insert_message AFTER INSERT ON messages BEGIN UPDATE chat_rooms SET message_count = message_count + 1 WHERE room_id = new.room_id; END");
        }
    };

    private static volatile RoomDatabaseManager sInstance = null;

    public static RoomDatabaseManager getInstance() {
        RoomDatabaseManager instance = sInstance;
        if (instance == null) {
            synchronized (RoomDatabaseManager.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new RoomDatabaseManager();
                }
            }
        }
        return instance;
    }

    public synchronized void initForUser(Context context) {
        if (mUserDatabase == null) {
            mUserDatabase = Room.databaseBuilder(context, UserDatabase.class, "user.dat")
                    .addMigrations(mUserDBMigration_1_2)
                    .addCallback(mUserDBCallback)
                    .build();

            mChatMessageDao = mUserDatabase.getChatMessageDAO();
            mChatConversationDao = mUserDatabase.getChatConversationDAO();
            mChatProfileDao = mUserDatabase.getChatProfileDAO();
        }
    }

    public Observable<ChatConversation> asyncGetContactConversation(final int contactId) {
        return Observable.fromCallable(() -> {
            ChatConversationDBO dbo = mChatConversationDao.findConversationByFriendId(contactId);
            return FROM_DBO_TO_CONVERSATION.map(dbo);
        });
    }

    public LiveData<List<ChatConversationDBO>> getHomeConversation() {
        return mChatConversationDao.getHomeConversations();
    }

    public ChatMessageDBO findMessageById(long dboId) {
        return mChatMessageDao.findMessageByDBOId(dboId);
    }

    public LiveData<List<ChatMessageDBO>> getMessages(final long roomId) {
        return mChatMessageDao.getMessageInRoom(roomId);
    }

    public Observable<Long> asyncInsertChatMessage(ChatMessage chatMessage) {
        return Observable.fromCallable(() -> {
            updateOrCreateConversation(chatMessage.getChatItem(), (int) chatMessage.getFriendId());
            long insertedMessageId = mChatMessageDao.insertMessage(FROM_CHAT_MESSAGE_TO_DBO.map(chatMessage));
            return insertedMessageId;
        });
    }

    public ChatMessage insertChatMessage(ZLive.ZAPIPrivateChatItem chatItem, int contactId, int unreadFlag) {
        Log.e(TAG, "insertChatMessage");

        ChatMessageDBO dbo = mChatMessageDao.findMessageBy(chatItem.getMessageId(), chatItem.getChannelId());

        if (dbo != null) {
            return null;
        }

        long conversationId = updateOrCreateConversation(chatItem, contactId);

        ChatMessage chatMessage = new ChatMessage(chatItem);
        chatMessage.setRoomId(conversationId);
        chatMessage.setFriendId(contactId);
        chatMessage.setRead(unreadFlag == 0);

        long id = mChatMessageDao.insertMessage(FROM_CHAT_MESSAGE_TO_DBO.map(chatMessage));

        if (id > 0) {
            chatMessage.setId(id);
            return chatMessage;
        } else {
            return null;
        }
    }

    private long updateOrCreateConversation(ZLive.ZAPIPrivateChatItem chatItem, int contactId) {
        ChatConversationDBO conversationDBO = mChatConversationDao.findConversationByFriendId(contactId);

        if (conversationDBO == null) {
            ChatConversation conversation = new ChatConversation(contactId, chatItem, 0, 0);
            conversationDBO = FROM_CONVERSATION_TO_DBO.map(conversation);
            conversationDBO.createdTime = System.currentTimeMillis();
            return mChatConversationDao.insertConversation(conversationDBO);
        } else {
            conversationDBO.snippet = chatItem.getMessage();
            conversationDBO.modifiedTime = System.currentTimeMillis();
            conversationDBO.channelId = chatItem.getChannelId();

            if (!ChatConversation.isUserCreatedConversation(conversationDBO.conversationType)
                    || !ChatConversation.isStrangerConversation(chatItem.getChannelType())) {
                conversationDBO.conversationType = chatItem.getChannelType();
            }

            mChatConversationDao.updateConversation(conversationDBO);
            return conversationDBO.roomId;
        }
    }

    public void updateMessage(ChatMessageDBO dbo) {
        mChatMessageDao.updateMessage(dbo);
    }

    public Completable asyncDeleteConversation(final long roomId) {
        return Completable.fromAction(() -> {
            mChatConversationDao.deleteConversation(roomId);
            mChatMessageDao.deleteMessage(roomId);
        });
    }

    public List<ChatProfileDBO> getProfiles() {
        return mChatProfileDao.getAllProfiles();
    }

    public boolean save(ChatProfileDBO dbo) {
        if (dbo == null) {
            return false;
        }

        return mChatProfileDao.insert(dbo) != 1;
    }

    public Completable asyncMarkMessagesRead(final long roomId) {
        return Completable.fromAction(() -> mChatMessageDao.markMessageRead(roomId));
    }

    public Observable<Integer> asyncGetUnreadConversationCount() {
        return Observable.fromCallable(() -> mChatConversationDao.getUnreadConversationsCount());
    }

    public Completable asyncDeleteConversationIfEmpty(final long roomId) {
        return Completable.fromAction(() -> {
            ChatConversationDBO conversation = mChatConversationDao.findConversationById(roomId);

            if (conversation != null && conversation.messageCount == 0) {
                mChatConversationDao.deleteConversation(conversation);
            }
        });
    }

    public Observable<ChatConversation> asyncGetOrCreateContactConversation(final int contactId) {
        return Observable.fromCallable(() -> getOrCreateContactConversation(contactId));
    }

    public ChatConversation getOrCreateContactConversation(final int contactId) {
        ChatConversationDBO dbo = mChatConversationDao.findConversationByFriendId(contactId);

        return updateOrCreateConversation(contactId, dbo, ChatConversation.TYPE_USER_CREATED);
    }

    private ChatConversation updateOrCreateConversation(int contactId, ChatConversationDBO dbo, int channelType) {
        ChatConversation conversation;
        if (dbo == null) {
            conversation = new ChatConversation(0,
                    0,
                    contactId,
                    "",
                    System.currentTimeMillis(),
                    0,
                    0,
                    channelType);

            dbo = FROM_CONVERSATION_TO_DBO.map(conversation);
            long conversationId = mChatConversationDao.insertConversation(dbo);
            conversation.setId(conversationId);
        } else {
            conversation = FROM_DBO_TO_CONVERSATION.map(dbo);
        }

        return conversation;
    }
}
