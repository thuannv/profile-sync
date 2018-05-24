package com.vng.datasync.ui.chat;

import com.google.protobuf.ByteString;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.data.remote.Commands;
import com.vng.datasync.data.remote.MessageHelper;
import com.vng.datasync.data.remote.websocket.WebSocketManager;
import com.vng.datasync.ui.BasePresenter;
import com.vng.datasync.util.Logger;
import com.vng.datasync.util.RxUtils;

import rx.subscriptions.CompositeSubscription;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 14/08/2017
 */

public class ConversationsPresenter extends BasePresenter<ConversationsView> {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(ConversationsPresenter.class, BuildConfig.DEBUG && DEBUG);

    private final WebSocketManager mConnectionManager;

    private final CompositeSubscription mCompositeSubscription;

    public ConversationsPresenter() {
        mConnectionManager = WebSocketManager.getInstance();

        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        RxUtils.unsubscribe(mCompositeSubscription);

        super.detachView();
    }

    public void getOfflineMessages() {
            byte[] message = MessageHelper.createMessage(Commands.CMD_CHAT_PRIVATE_REQUEST_UNREAD, ByteString.EMPTY);

            mConnectionManager.send(message);

            L.d("Offline messages-Send request offline channels success");
    }
}
