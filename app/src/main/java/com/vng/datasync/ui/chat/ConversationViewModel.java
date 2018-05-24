package com.vng.datasync.ui.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.local.room.ChatRepository;

import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 26/04/2018
 */
public class ConversationViewModel extends ViewModel {
    private ChatRepository mRepository;

    private LiveData<List<ChatConversation>> mHomeConversations;

    public ConversationViewModel() {
        mRepository = ChatRepository.getInstance();
    }

    public void init() {
        if (mHomeConversations == null) {
            mHomeConversations = mRepository.getHomeConversation();
        }
    }

    public LiveData<List<ChatConversation>> getHomeConversation() {
        return mHomeConversations;
    }

    public void asyncDeleteConversation(long roomId) {
        mRepository.asyncDeleteConversation(roomId);
    }
}
