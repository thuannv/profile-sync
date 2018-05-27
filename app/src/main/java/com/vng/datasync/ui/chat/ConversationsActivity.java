package com.vng.datasync.ui.chat;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.vng.datasync.DataSyncApp;
import com.vng.datasync.R;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.event.Event;
import com.vng.datasync.event.EventDispatcher;
import com.vng.datasync.event.EventListener;
import com.vng.datasync.ui.chat.privatechat.contactchat.ContactChatActivity;
import com.vng.datasync.ui.widget.DrawableDividerItemDecoration;
import com.vng.datasync.util.DialogUtils;
import com.vng.datasync.util.ProfileManager;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 14/08/2017
 */

public class ConversationsActivity extends AppCompatActivity implements ConversationsView {

    private static final int HANDLER_BASE_MESSAGE = 0;

    private static final int HANDLER_MESSAGE_NEW_CHAT_MESSAGE = HANDLER_BASE_MESSAGE + 1;

    private static final int HANDLER_MESSAGE_SYNC_PROFILE_DONE = HANDLER_BASE_MESSAGE + 2;

    private static final int HANDLER_MESSAGE_NEW_FRIEND_REQUEST = HANDLER_BASE_MESSAGE + 3;

    private static final int HANDLER_MESSAGE_DELETED_CONVERSATION = HANDLER_BASE_MESSAGE + 4;

    private static final int HANDLER_MESSAGE_SYNC_OFFLINE_MSG_COMPLETED = HANDLER_BASE_MESSAGE + 5;

    @BindView(R.id.conversation_list)
    RecyclerView mConversationList;

    private Unbinder mUnbinder;

    private ConversationsPresenter mPresenter;

    private ConversationAdapter mAdapter;

    private ConversationViewEventListener mEventListener;

    private Dialog mConfirmDeleteDialog;

    private EventHandler mHandler;

    private ConversationViewModel mConversationViewModel;

//    public static ConversationsActivity newInstance() {
//        ConversationsActivity f = new ConversationsActivity();
//        f.setArguments(new Bundle());
//        return f;
//    }

    public static Intent intentFor(Context context) {
        return new Intent(context, ConversationsActivity.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_conversation_home);

        mUnbinder = ButterKnife.bind(this);

        mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
        mConversationViewModel.init();

        initConversationList();

        mPresenter = new ConversationsPresenter();
        mPresenter.attachView(this);
        mPresenter.getOfflineMessages();

        ProfileManager.getInstance().init(DataSyncApp.getInstance().getApplicationContext());

        mHandler = new EventHandler(this);
    }

    private void registerEventListener() {
        mEventListener = new ConversationViewEventListener(mHandler);
        EventDispatcher.getInstance().addListener(Event.PROFILE_SYNC_SUCCESS, mEventListener);
        EventDispatcher.getInstance().addListener(Event.PROFILE_SYNC_FAILURE, mEventListener);
        EventDispatcher.getInstance().addListener(Event.NEW_FRIEND_REQUEST, mEventListener);
        EventDispatcher.getInstance().addListener(Event.NEW_PRIVATE_CHAT_MESSAGE_EVENT, mEventListener);
        EventDispatcher.getInstance().addListener(Event.DELETED_CONVERSATION, mEventListener);
        EventDispatcher.getInstance().addListener(Event.SYNC_OFFLINE_MESSAGES_COMPLETED, mEventListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        registerEventListener();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        unregisterEventListener();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();

        if (mUnbinder != null) {
            mUnbinder.unbind();
        }

        if (mConfirmDeleteDialog != null) {
            DialogUtils.dismiss(mConfirmDeleteDialog);
            mConfirmDeleteDialog = null;
        }

        super.onDestroy();
    }

    private void unregisterEventListener() {
        EventDispatcher.getInstance().removeListener(Event.PROFILE_SYNC_FAILURE, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.PROFILE_SYNC_SUCCESS, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.NEW_FRIEND_REQUEST, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.NEW_PRIVATE_CHAT_MESSAGE_EVENT, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.DELETED_CONVERSATION, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.SYNC_OFFLINE_MESSAGES_COMPLETED, mEventListener);

        mEventListener = null;
    }

    private void initConversationList() {
        createListAdapter();

        Context applicationContext = DataSyncApp.getInstance().getApplicationContext();

        DrawableDividerItemDecoration decoration = new DrawableDividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.divider_1_e8e8e9));
        decoration.setIncludeEdge(true);

        mConversationList.setHasFixedSize(false);
        mConversationList.setLayoutManager(new LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false));
        mConversationList.addItemDecoration(decoration);
        mConversationList.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = createItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(mConversationList);

        mConversationViewModel.getHomeConversation().observe(this,
                homeConversation -> mAdapter.setData(homeConversation));
    }

    private void createListAdapter() {
        mAdapter = new ConversationAdapter(this);

        mAdapter.setConversationClickHandler(conversation -> startActivity(ContactChatActivity.createIntentForActivity(this, conversation)));
    }

    private ItemTouchHelper createItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int viewPosition = viewHolder.getAdapterPosition();

                final ChatConversation conversationToDelete = mAdapter.getItem(viewPosition);

                if (conversationToDelete == null) {
                    mAdapter.notifyItemChanged(viewPosition);
                    return;
                }

                final long roomId = conversationToDelete.getId();
                showConfirmDeleteDialog(roomId);
            }
        });
    }

    private void showConfirmDeleteDialog(final long roomId) {
        if (DialogUtils.isDialogShowing(mConfirmDeleteDialog)) {
            return;
        }

        DialogInterface.OnClickListener positiveClickListener = (dialog, which) -> mConversationViewModel.asyncDeleteConversation(roomId);

        DialogInterface.OnClickListener negativeClickListener = (dialog, which) -> {
            int position = mAdapter.indexOf(roomId);
            mAdapter.notifyItemChanged(position);
        };

        mConfirmDeleteDialog = DialogUtils.showConfirmDeleteConversationDialog(this, positiveClickListener, negativeClickListener);
    }

    public void setHomeConversations(List<ChatConversation> chatConversations) {
        mAdapter.setData(chatConversations);
    }

    private void updateSyncProfileStatus(final int profileId) {
        mAdapter.updateProfileSyncStatus(profileId);
    }

    /**
     * {@link EventHandler}
     */
    private final static class EventHandler extends Handler {

        private final WeakReference<ConversationsActivity> mRef;

        public EventHandler(ConversationsActivity ref) {
            super(Looper.getMainLooper());
            mRef = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            ConversationsActivity fragment = mRef.get();

            if (fragment == null) {
                return;
            }

            switch (msg.what) {
                case HANDLER_MESSAGE_SYNC_PROFILE_DONE:
                    fragment.updateSyncProfileStatus(msg.arg1);
                    break;

                case HANDLER_MESSAGE_NEW_FRIEND_REQUEST:
//                    fragment.updateRowFriendRequests();
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    /**
     * {@link ConversationViewEventListener}
     */
    private static class ConversationViewEventListener implements EventListener {

        private final EventHandler mHandler;

        protected ConversationViewEventListener(EventHandler handler) {
            mHandler = handler;
        }

        @Override
        public void onEvent(int id, Object... args) {
            if (mHandler != null) {
                if (id == Event.PROFILE_SYNC_FAILURE || id == Event.PROFILE_SYNC_SUCCESS) {
                    notifySyncProfileDone((Integer) args[0]);
                } else if (id == Event.NEW_FRIEND_REQUEST) {
//                    mHandler.sendEmptyMessage(HANDLER_MESSAGE_NEW_FRIEND_REQUEST);
                } else if (id == Event.SYNC_OFFLINE_MESSAGES_COMPLETED) {
                    mHandler.sendEmptyMessage(HANDLER_MESSAGE_SYNC_OFFLINE_MSG_COMPLETED);
                }
            }
        }

        private void notifySyncProfileDone(int profileId) {
            Message message = mHandler.obtainMessage(HANDLER_MESSAGE_SYNC_PROFILE_DONE);
            message.arg1 = profileId;
            mHandler.sendMessage(message);
        }
    }
}
