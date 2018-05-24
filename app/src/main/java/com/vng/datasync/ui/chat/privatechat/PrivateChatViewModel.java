package com.vng.datasync.ui.chat.privatechat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.local.room.ChatRepository;
import com.vng.datasync.data.remote.websocket.WebSocketManager;
import com.vng.datasync.util.Logger;

import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 27/04/2018
 */
public class PrivateChatViewModel extends ViewModel {
    private static final Logger L = Logger.getLogger(PrivateChatViewModel.class.getSimpleName(), true);

    private WebSocketManager mWebSocketManager;

    private ChatRepository mRepository;

    private long mCurrentRoomId;

    private LiveData<List<ChatMessage>> mMessagesList;

    public PrivateChatViewModel() {
        mWebSocketManager = WebSocketManager.getInstance();
        mRepository = ChatRepository.getInstance();
    }

    public void setCurrentRoomId(long roomId) {
        mCurrentRoomId = roomId;
    }

    public LiveData<List<ChatMessage>> getMessagesList() {
        if (mMessagesList == null) {
            mMessagesList = mRepository.getMessagesListOfRoom(mCurrentRoomId);
        }
        return mMessagesList;
    }

    public void markMessagesRead(int roomId) {
        mRepository.markMessagesRead(roomId);
    }

    public void asyncDeleteConversation(long roomId) {
        mRepository.asyncDeleteConversation(roomId);
    }

    public void deleteConversationIfEmpty(long roomId) {
        mRepository.deleteConversationIfEmpty(roomId);
    }

    public void refreshData() {
        mMessagesList = mRepository.getMessagesListOfRoom(mCurrentRoomId);
    }
}
