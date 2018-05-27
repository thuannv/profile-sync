package com.vng.datasync.ui.chat.privatechat.contactchat;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.DataSyncApp;
import com.vng.datasync.GlideApp;
import com.vng.datasync.R;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.event.Event;
import com.vng.datasync.event.EventDispatcher;
import com.vng.datasync.event.EventListener;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.remote.ChatHandler;
import com.vng.datasync.ui.chat.privatechat.PrivateChatAdapter;
import com.vng.datasync.ui.chat.privatechat.PrivateChatViewModel;
import com.vng.datasync.ui.widget.CircleImageView;
import com.vng.datasync.util.AndroidUtilities;
import com.vng.datasync.util.DialogUtils;
import com.vng.datasync.util.ProfileManager;
import com.vng.datasync.util.SimpleTextWatcher;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 13/09/2017
 */

public class ContactChatActivity extends AppCompatActivity implements ContactChatView,
        PrivateChatAdapter.AvatarLoader {

    private static final String BUNDLE_CURRENT_CONVERSATION = "key_current_conversation";

    private static final int MESSAGE_NEW_CHAT_MESSAGE = 1;

    private static final int MESSAGE_SYNC_PROFILE_DONE = 2;

    private static final int MESSAGE_UPDATE_SENT_STATUS = 3;

    private static final int MESSAGE_NOTIFY_BLOCKED_CHAT = 4;

    private static final int MESSAGE_SYNC_OFFLINE_CHANNEL_COMPLETED = 5;

    private static final int AVATAR_SIZE = AndroidUtilities.dp(32);

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.contact_avatar)
    CircleImageView mContactAvatar;

    @BindView(R.id.loading_avatar)
    View mLoadingAvatar;

    @BindView(R.id.contact_name)
    TextView mContactName;

    @BindView(R.id.messages_list)
    RecyclerView mMessagesList;

    @BindView(R.id.input_message_overlay)
    View mInputMessageOverlay;

    @BindView(R.id.input_message)
    EditText mMessageEditText;

    @BindView(R.id.btn_send)
    ImageView mSendBtn;

    private final Object mLock = new Object();

    private final ProfileManager mProfileManager = ProfileManager.getInstance();

    private ContactChatPresenter mPresenter;

    private PrivateChatAdapter mAdapter;

    private ChatConversation mCurrentConversation;

    private Profile mCurrentContact;

    private Unbinder mUnbinder;

    private AlertDialog mDeleteConversationDialog;

    private Dialog mPromptDeleteDialog;

    private Dialog mBlockedDialog;

    private EventHandler mHandler;

    private EventListener mEventListener;

    private PrivateChatViewModel mPrivateChatViewModel;

    private Dialog mResponseFriendRequestDialog;

    private Observer<List<ChatMessage>> mMessageObserver = this::setChatMessages;

    public static Intent createIntentForActivity(Context context, ChatConversation conversation) {
        Intent intent = new Intent(context, ContactChatActivity.class);
        intent.putExtra(BUNDLE_CURRENT_CONVERSATION, conversation);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentConversation = getIntent().getParcelableExtra(BUNDLE_CURRENT_CONVERSATION);

        if (mCurrentConversation == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_private_chat);

        mUnbinder = ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black);
        }

        mCurrentContact = mProfileManager.get(mCurrentConversation.getContactId());

        mHandler = new EventHandler(this);

        mProfileManager.init(this);

        setAvatarAndTitle();

        setupMessagesList();

        setupInputLayout();

        mPrivateChatViewModel = ViewModelProviders.of(this).get(PrivateChatViewModel.class);
        mPrivateChatViewModel.setCurrentRoomId((int) mCurrentConversation.getId());
        mPrivateChatViewModel.getMessagesList().observe(this, mMessageObserver);

        mPresenter = new ContactChatPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mCurrentConversation = intent.getParcelableExtra(BUNDLE_CURRENT_CONVERSATION);

        if (mCurrentConversation == null) {
            finish();
        }

        mCurrentContact = mProfileManager.get(mCurrentConversation.getContactId());

        ChatHandler.setCurrentVisibleChannel(mCurrentConversation.getContactId());

        setAvatarAndTitle();

        LiveData<List<ChatMessage>> messageLiveData = mPrivateChatViewModel.getMessagesList();
        messageLiveData.removeObserver(mMessageObserver);
        mPrivateChatViewModel.setCurrentRoomId(mCurrentConversation.getId());
        mPrivateChatViewModel.refreshData();
        messageLiveData = mPrivateChatViewModel.getMessagesList();
        messageLiveData.observe(this, mMessageObserver);


        mAdapter.setCurrentContact(mCurrentContact);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ChatHandler.setCurrentVisibleChannel(mCurrentConversation.getContactId());

        registerListener();
    }

    @Override
    protected void onStop() {
        unregisterListener();

        ChatHandler.setCurrentVisibleChannel(0);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }

        if (mPrivateChatViewModel != null) {
            mPrivateChatViewModel.deleteConversationIfEmpty(mCurrentConversation.getId());
        }

        if (mUnbinder != null) {
            mUnbinder.unbind();
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        dismissDialogs();

        super.onDestroy();
    }

    @Override
    public void finish() {
//        Intent intent = getIntent();
//        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
//            Intent mainActivityIntent = new Intent(this, MainActivity.class);
//            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(mainActivityIntent);
//        }
        super.finish();
    }

    private void dismissDialogs() {
        if (mDeleteConversationDialog != null) {
            DialogUtils.dismiss(mDeleteConversationDialog);
            mDeleteConversationDialog = null;
        }

        if (mPromptDeleteDialog != null) {
            DialogUtils.dismiss(mPromptDeleteDialog);
            mPromptDeleteDialog = null;
        }

        if (mBlockedDialog != null) {
            DialogUtils.dismiss(mBlockedDialog);
            mBlockedDialog = null;
        }
    }

    private void unregisterListener() {
        EventDispatcher.getInstance().removeListener(Event.PROFILE_SYNC_FAILURE, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.PROFILE_SYNC_SUCCESS, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.NEW_PRIVATE_CHAT_MESSAGE_EVENT, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.BLOCKED_PRIVATE_CHAT_EVENT, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.SEND_CHAT_RESULT_EVENT, mEventListener);
        EventDispatcher.getInstance().removeListener(Event.SYNC_OFFLINE_CHANNEL_COMPLETE, mEventListener);

        mEventListener = null;
    }

    private void registerListener() {
        mEventListener = new PrivateChatEventListener(mHandler);

        EventDispatcher.getInstance().addListener(Event.PROFILE_SYNC_FAILURE, mEventListener);
        EventDispatcher.getInstance().addListener(Event.PROFILE_SYNC_SUCCESS, mEventListener);
        EventDispatcher.getInstance().addListener(Event.NEW_PRIVATE_CHAT_MESSAGE_EVENT, mEventListener);
        EventDispatcher.getInstance().addListener(Event.BLOCKED_PRIVATE_CHAT_EVENT, mEventListener);
        EventDispatcher.getInstance().addListener(Event.SEND_CHAT_RESULT_EVENT, mEventListener);
        EventDispatcher.getInstance().addListener(Event.SYNC_OFFLINE_CHANNEL_COMPLETE, mEventListener);
    }

    private void setAvatarAndTitle() {
        if (mCurrentConversation == null) {
            return;
        }

        final int contactId = mCurrentConversation.getContactId();

        setAvatarImage(contactId);

        setTitleText(contactId);
    }

    private void setTitleText(int contactId) {
        mContactName.setText(getString(R.string.display_user_id, String.valueOf(contactId)));

        if (mCurrentContact.isSynced()) {
            mContactName.setText(mCurrentContact.getDisplayName());
        }
    }

    private void setAvatarImage(int contactId) {
        if (mCurrentContact.isEmpty()) {
            mLoadingAvatar.setVisibility(View.GONE);
            mContactAvatar.setImageResource(R.drawable.avatar_default);
            mProfileManager.sync(contactId);
        }

        if (mProfileManager.isSyncing(contactId)) {
            mLoadingAvatar.setVisibility(View.VISIBLE);
        }

        if (mCurrentContact.isSynced()) {
            mLoadingAvatar.setVisibility(View.GONE);

            GlideApp.with(this)
                    .load(mCurrentContact.getAvatar())
                    .override(AVATAR_SIZE)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .placeholder(R.color.silver)
                    .error(R.drawable.avatar_default)
                    .into(mContactAvatar);
        }
    }

    private void setupInputLayout() {
        mMessageEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mSendBtn.setImageResource(R.drawable.ic_send_mess_on);
                } else {
                    mSendBtn.setImageResource(R.drawable.ic_send_mess_off);
                }
            }
        });
    }

    private void setupMessagesList() {
        createAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        mMessagesList.setLayoutManager(layoutManager);
        mMessagesList.setAdapter(mAdapter);
    }

    private void createAdapter() {
        mAdapter = new PrivateChatAdapter();

        mAdapter.setCurrentContact(mCurrentContact);
        mAdapter.setAvatarLoader(this);
        mAdapter.setRetryClickListener(chatMessage -> mPresenter.resendMessage(chatMessage, getPredictedChatCreatedTime()));
    }

    @OnClick(R.id.btn_send)
    public void onSendClick() {
        String message = mMessageEditText.getText().toString();

        if (!isValidMessage(message)) {
            return;
        }

        int channelType = mCurrentConversation.getConversationType() == ChatConversation.TYPE_USER_CREATED ?
                ChatConversation.TYPE_STRANGER :
                mCurrentConversation.getConversationType();

        mPresenter.sendMessage(mCurrentContact.getUserId(), mCurrentConversation.getId(), message, channelType, getPredictedChatCreatedTime());

        mMessageEditText.setText("");
    }

    private boolean isValidMessage(String message) {
        return !TextUtils.isEmpty(message) && mCurrentConversation != null;
    }

    private long getPredictedChatCreatedTime() {
        ChatMessage lastChatMessage = mAdapter.getItem(mAdapter.getItemCount() - 1);
        long now = System.currentTimeMillis();
        return lastChatMessage == null || lastChatMessage.getCreatedTime() < now ? now : lastChatMessage.getCreatedTime() + 1;
    }

    @Override
    public void setChatMessages(List<ChatMessage> chatMessages) {
        if (mCurrentConversation == null) {
            return;
        }

        mAdapter.setItems(chatMessages);

        smoothScrollToBottom();

        mPrivateChatViewModel.markMessagesRead((int) mCurrentConversation.getId());

        cancelNotificationIfAny();
    }

    private void cancelNotificationIfAny() {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(DataSyncApp.getInstance().getApplicationContext());
        managerCompat.cancel(mCurrentConversation.getContactId());
    }

    private void smoothScrollToBottom() {
            if (mAdapter.getItemCount() > 0 && mMessagesList != null) {
                mMessagesList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
    }

    @Override
    public void loadAvatar(ImageView imageView) {
        GlideApp.with(this)
                .load(mCurrentContact.getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .error(R.drawable.avatar_default)
                .placeholder(R.color.silver)
                .override(AVATAR_SIZE)
                .into(imageView);
    }

    private void onSyncProfileDone() {
        if (mCurrentConversation == null) {
            return;
        }

        mCurrentContact = mProfileManager.get(mCurrentConversation.getContactId());

        setAvatarAndTitle();

        mAdapter.setCurrentContact(mCurrentContact);

        mAdapter.notifyDataSetChanged();
    }

    private void updateSentMessageStatus(@ChatMessage.ChatMessageState final int state,
                                         final int requestId,
                                         final ZLive.ZAPIPrivateChatItem chatItem) {
        if (chatItem == null || mCurrentConversation == null) {
            return;
        }

        mAdapter.updateMessage(state, requestId, chatItem);

        int channelType = chatItem.getChannelType();

        if (state == ChatMessage.STATE_SEND_SUCCESS) {
            if (ChatConversation.TYPE_USER_CREATED != mCurrentConversation.getConversationType()
                    || ChatConversation.TYPE_STRANGER != channelType) {
                mCurrentConversation.setConversationType(channelType);
            }
        }
    }

    private void notifySyncOfflineChannelCompleted(List<ChatMessage> offlineMessages) {
        mAdapter.insert(offlineMessages);
    }

    /**
     * {@link EventHandler}
     */
    private final static class EventHandler extends Handler {

        private final WeakReference<ContactChatActivity> mRef;

        public EventHandler(ContactChatActivity ref) {
            super(Looper.getMainLooper());
            mRef = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            ContactChatActivity activity = mRef.get();

            if (activity == null) {
                return;
            }

            switch (msg.what) {
                case MESSAGE_SYNC_PROFILE_DONE:
                    activity.onSyncProfileDone();
                    break;

                case MESSAGE_UPDATE_SENT_STATUS:
                    activity.updateSentMessageStatus(msg.arg1, msg.arg2, (ZLive.ZAPIPrivateChatItem) msg.obj);
                    break;

                case MESSAGE_SYNC_OFFLINE_CHANNEL_COMPLETED:
                    activity.notifySyncOfflineChannelCompleted((List<ChatMessage>) msg.obj);

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    /**
     * {@link PrivateChatEventListener}
     */
    private static class PrivateChatEventListener implements EventListener {

        private final EventHandler mHandler;

        private PrivateChatEventListener(EventHandler handler) {
            mHandler = handler;
        }

        @Override
        public void onEvent(int id, Object... args) {
            if (mHandler == null) {
                return;
            }

            switch (id) {
                case Event.PROFILE_SYNC_FAILURE:
                    // Fall through
                case Event.PROFILE_SYNC_SUCCESS:
                    notifySyncProfileDone();
                    break;

                case Event.SEND_CHAT_RESULT_EVENT:
                    notifyUpdateSentStatus(((int) args[0]), ((int) args[1]), (ZLive.ZAPIPrivateChatItem) args[2]);
                    break;

                case Event.NEW_PRIVATE_CHAT_MESSAGE_EVENT:
                    notifyNewMessageIfNeeded((ChatMessage) args[0]);
                    break;

                case Event.BLOCKED_PRIVATE_CHAT_EVENT:
                    notifyBlockedPrivateChat();
                    break;

                case Event.SYNC_OFFLINE_CHANNEL_COMPLETE:
                    notifySyncOfflineChannelCompleted((List<ChatMessage>) args[0]);

                default:
                    break;
            }
        }

        private void notifySyncOfflineChannelCompleted(List<ChatMessage> offlineMessages) {
            Message message = mHandler.obtainMessage(MESSAGE_SYNC_OFFLINE_CHANNEL_COMPLETED);
            message.obj = offlineMessages;
            message.sendToTarget();
        }

        private void notifySyncProfileDone() {
            mHandler.sendEmptyMessage(MESSAGE_SYNC_PROFILE_DONE);
        }

        private void notifyUpdateSentStatus(int state, int requestId, ZLive.ZAPIPrivateChatItem zapiPrivateChatItem) {
            Message message = mHandler.obtainMessage();
            message.what = MESSAGE_UPDATE_SENT_STATUS;
            message.arg1 = state;
            message.arg2 = requestId;
            message.obj = zapiPrivateChatItem;
            mHandler.sendMessage(message);
        }

        private void notifyNewMessageIfNeeded(ChatMessage chatMessage) {
            if (ChatHandler.isChannelVisible(chatMessage.getFriendId())) {
                mHandler.obtainMessage(MESSAGE_NEW_CHAT_MESSAGE, chatMessage).sendToTarget();
            }
        }

        private void notifyBlockedPrivateChat() {
            mHandler.sendEmptyMessage(MESSAGE_NOTIFY_BLOCKED_CHAT);
        }
    }
}
