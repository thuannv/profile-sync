package com.vng.datasync.data.local.room;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;

import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.model.roomdb.ChatConversationDBO;
import com.vng.datasync.data.model.roomdb.ChatMessageDBO;
import com.vng.datasync.util.CollectionUtils;
import com.vng.datasync.util.Logger;
import com.vng.datasync.util.SimpleCompletableSubscriber;

import java.util.ArrayList;
import java.util.List;

import rx.schedulers.Schedulers;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 26/04/2018
 */
public final class ChatRepository {
    private static final Logger L = Logger.getLogger(ChatRepository.class, true);

    private RoomDatabaseManager mRoomDatabaseManager;

    private static volatile ChatRepository sInstance = null;

    private ChatRepository() {
        mRoomDatabaseManager = RoomDatabaseManager.getInstance();
    }

    public static ChatRepository getInstance() {
        ChatRepository instance = sInstance;
        if (instance == null) {
            synchronized (ChatRepository.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new ChatRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<ChatConversation>> getHomeConversation() {
        LiveData<List<ChatConversationDBO>> homeConversation = mRoomDatabaseManager.getHomeConversation();
//        MediatorLiveData<List<ChatConversationDBO>> liveData = new MediatorLiveData<>();
//        liveData.addSource(homeConversation, new DistinctChatConversationObserver(liveData));
        return Transformations.map(homeConversation, dboList -> {
            List<ChatConversation> ret = new ArrayList<>();
            for (ChatConversationDBO dbo : dboList) {
                ret.add(Mappers.FROM_DBO_TO_CONVERSATION.map(dbo));
            }
            return ret;
        });
    }

    public LiveData<List<ChatMessage>> getMessagesListOfRoom(long roomId) {
        LiveData<List<ChatMessageDBO>> messages = mRoomDatabaseManager.getMessages(roomId);
//        MediatorLiveData<List<ChatMessageDBO>> liveData = new MediatorLiveData<>();
//        liveData.addSource(messages, new DistinctChatMessageObserver(liveData));
        return Transformations.map(messages, this::transformListChatDBO);
    }

    public void markMessagesRead(long roomId) {
        mRoomDatabaseManager.asyncMarkMessagesRead(roomId)
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleCompletableSubscriber() {
                    @Override
                    public void onError(Throwable e) {
                        L.e(e.toString());
                    }
                });
    }

    public void asyncDeleteConversation(long roomId) {
        mRoomDatabaseManager.asyncDeleteConversation(roomId)
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleCompletableSubscriber() {
                    @Override
                    public void onError(Throwable e) {
                        L.e(e.toString());
                    }
                });
    }

    public void deleteConversationIfEmpty(long roomId) {
        mRoomDatabaseManager.asyncDeleteConversationIfEmpty(roomId)
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleCompletableSubscriber() {
                    @Override
                    public void onError(Throwable e) {
                        L.e(e.toString());
                    }
                });
    }

    private List<ChatMessage> transformListChatDBO(List<ChatMessageDBO> dboList) {
        List<ChatMessage> ret = new ArrayList<>();
        for (ChatMessageDBO dbo : dboList) {
            ret.add(Mappers.FROM_DBO_TO_CHAT_MESSAGE.map(dbo));
        }
        return ret;
    }

    private List<ChatConversation> transformListConversationDBO(List<ChatConversationDBO> dboList) {
        List<ChatConversation> ret = new ArrayList<>();
        for (ChatConversationDBO dbo : dboList) {
            ret.add(Mappers.FROM_DBO_TO_CONVERSATION.map(dbo));
        }
        return ret;
    }

    /**
     * {@link DistinctChatMessageObserver}
     */
    private static class DistinctChatMessageObserver implements Observer<List<ChatMessageDBO>> {

        private boolean mIsInitialized = false;

        private List<ChatMessageDBO> mLastObject;

        private MediatorLiveData<List<ChatMessageDBO>> mLiveData;

        private DistinctChatMessageObserver(MediatorLiveData<List<ChatMessageDBO>> liveData) {
            mLiveData = liveData;
        }

        @Override
        public void onChanged(@Nullable List<ChatMessageDBO> object) {
            if (mLiveData != null) {
                if (!mIsInitialized) {
                    mIsInitialized = true;
                    mLastObject = object;
                    mLiveData.postValue(mLastObject);
                } else if ((object == null && mLastObject != null)
                        || (!CollectionUtils.isListsEqual(object, mLastObject, Comparators.CHAT_MESSAGE_DBO_COMPARATOR))) {
                    mLastObject = object;
                    mLiveData.postValue(mLastObject);
                }
            }
        }
    }

    private class DistinctChatConversationObserver implements Observer<List<ChatConversationDBO>> {
        private boolean mIsInitialized = false;

        private List<ChatConversationDBO> mLastObject;

        private MediatorLiveData<List<ChatConversationDBO>> mLiveData;

        public DistinctChatConversationObserver(MediatorLiveData<List<ChatConversationDBO>> liveData) {
            mLiveData = liveData;
        }

        @Override
        public void onChanged(@Nullable List<ChatConversationDBO> object) {
            if (mLiveData != null) {
                if (!mIsInitialized) {
                    mIsInitialized = true;
                    mLastObject = object;
                    mLiveData.postValue(mLastObject);
                } else if ((object == null && mLastObject != null)
                        || (!CollectionUtils.isListsEqual(object, mLastObject, Comparators.CHAT_CONVERSATION_DBO_COMPARATOR))) {
                    mLastObject = object;
                    mLiveData.postValue(mLastObject);
                }
            }
        }
    }
}
