package com.vng.datasync.ui.chat.privatechat;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.DataSyncApp;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.local.User;
import com.vng.datasync.data.local.UserManager;
import com.vng.datasync.data.local.room.RoomDatabaseManager;
import com.vng.datasync.data.model.roomdb.ChatMessageDBO;
import com.vng.datasync.data.remote.Commands;
import com.vng.datasync.data.remote.MessageHelper;
import com.vng.datasync.data.remote.websocket.WebSocketManager;
import com.vng.datasync.ui.BasePresenter;
import com.vng.datasync.util.Logger;
import com.vng.datasync.util.RequestHelper;
import com.vng.datasync.util.RxUtils;
import com.vng.datasync.util.SimpleSubscriber;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 04/05/2018
 */

public abstract class PrivateChatPresenter<T extends PrivateChatView> extends BasePresenter<T> {
    private static final Logger L = Logger.getLogger(PrivateChatPresenter.class.getSimpleName(), true);

    private final Context mContext;

    private final User mUser;

    private final WebSocketManager mWebSocketConnectionManager;

    private final RoomDatabaseManager mRoomDatabaseManager;

    private final RequestHelper mRequestIdHelper;

    private final CompositeSubscription mCompositeSubscription;

    private final RequestHelper.OnRequestTimeOut mTimeOutCallback;

    public PrivateChatPresenter() {
        mContext = DataSyncApp.getInstance().getApplicationContext();

        mUser = UserManager.getCurrentUser();

        mWebSocketConnectionManager = WebSocketManager.getInstance();
        mWebSocketConnectionManager.connect();

        mRoomDatabaseManager = RoomDatabaseManager.getInstance();

        mRequestIdHelper = RequestHelper.getInstance();

        mCompositeSubscription = new CompositeSubscription();

        mTimeOutCallback = (requestId, chatItem) -> {
            L.d("Send message timeout, requestId = %d", requestId);
//        EventDispatcher.getInstance().post(Event.SEND_CHAT_RESULT_EVENT, ChatMessage.STATE_SEND_ERROR, requestId, chatItem);
            long messageId = mRequestIdHelper.getMessageIdOfRequestId(requestId);
            ChatMessageDBO dbo = mRoomDatabaseManager.findMessageById(messageId);
            dbo.state = ChatMessage.STATE_SEND_ERROR;
            mRoomDatabaseManager.updateMessage(dbo);
        };
    }

    @Override
    public void detachView() {
        RxUtils.unsubscribe(mCompositeSubscription);
        super.detachView();
    }

    public void getOfflineMessages(long lastModifiedTimestamp) {
            byte[] message = MessageHelper.createMessage(Commands.CMD_CHAT_PRIVATE_REQUEST_UNREAD, ByteString.EMPTY);

            mWebSocketConnectionManager.send(message);

            L.d("Offline messages-Send request offline channels success");
    }

    public void sendMessage(int contactId, long roomId, String message, int channelType, long predictedChatCreatedTime) {
        int requestId = mRequestIdHelper.getNextRequestId();

        ZLive.ZAPIPrivateChatItem chatItem = MessageHelper.createChatItem(mUser.getUserId(),
                message,
                contactId,
                predictedChatCreatedTime,
                channelType);

        final byte[] zapiMessage = MessageHelper.createMessage(Commands.CMD_SEND_PRIVATE_CHAT,
                chatItem.toByteString(),
                requestId);

        ChatMessage chatMessage = new ChatMessage(chatItem);
        chatMessage.setRequestId(requestId);
        chatMessage.setRoomId(roomId);
        chatMessage.setFriendId(contactId);
        chatMessage.setRead(true);

        Subscription subscription = mRoomDatabaseManager.asyncInsertChatMessage(chatMessage).subscribeOn(Schedulers.io())
                .subscribe(new SimpleSubscriber<Long>() {
                    @Override
                    public void onNext(Long messageId) {
                        mRequestIdHelper.addRequest(requestId,
                                messageId,
                                chatItem,
                                5000,
                                mTimeOutCallback);

                        mWebSocketConnectionManager.send(zapiMessage);
                    }

                    @Override
                    public void onError(Throwable e) {
                        L.e("Save message error, requestId = %d", requestId);
                    }
                });

        mCompositeSubscription.add(subscription);
    }

    public void resendMessage(ChatMessage chatMessage, long predictedChatCreatedTime) {
        int requestId = mRequestIdHelper.getNextRequestId();

        ZLive.ZAPIPrivateChatItem chatItem = MessageHelper.createChatItem(mUser.getUserId(),
                chatMessage.getMessage(),
                chatMessage.getReceiverId(),
                predictedChatCreatedTime,
                chatMessage.getChannelType());

        final byte[] zapiMessage = MessageHelper.createMessage(Commands.CMD_SEND_PRIVATE_CHAT,
                chatItem.toByteString(),
                requestId);

        mRequestIdHelper.addRequest(requestId,
                chatMessage.getId(),
                chatItem,
                5000,
                mTimeOutCallback);

        mWebSocketConnectionManager.send(zapiMessage);
    }
}
