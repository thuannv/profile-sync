package com.vng.datasync.ui.chat.privatechat;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vng.datasync.protobuf.ZLive;
import com.vng.datasync.R;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.local.User;
import com.vng.datasync.data.local.UserManager;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.ui.widget.CircleImageView;
import com.vng.datasync.util.CollectionUtils;
import com.vng.datasync.util.DateUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 14/09/2017
 */

public final class PrivateChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_UNKNOWN_OWNER_MESSAGE = 0;

    private static final int VIEW_TYPE_SELF_MESSAGE = VIEW_TYPE_UNKNOWN_OWNER_MESSAGE + 1;

    private static final int VIEW_TYPE_CONTACT_MESSAGE = VIEW_TYPE_SELF_MESSAGE + 1;

    private final Object mLock = new Object();

    private final SortedList<ChatMessage> mItems;

    private final User mUser;

    private Profile mCurrentContact;

    private OnAvatarClickListener mAvatarClickListener;

    private OnRetryClickListener mRetryClickListener;

    private AvatarLoader mAvatarLoader;

    private final AvatarLoader mInternalAvatarLoader = imageView -> {
        if (mAvatarLoader != null) {
            mAvatarLoader.loadAvatar(imageView);
        }
    };

    public PrivateChatAdapter() {
        mItems = new SortedList<>(ChatMessage.class, new PrivateChatSortedListCallback(this));
        mUser = UserManager.getCurrentUser();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (isSelfMessage(viewType)) {
            return new SelfMessageVH(inflater.inflate(R.layout.private_chat_self_message_layout, parent, false));
        }

        if (isContactMessage(viewType)) {
            return new ContactMessageVH(inflater.inflate(R.layout.private_chat_contact_message_layout, parent, false), mInternalAvatarLoader);
        }

        return new UnknownOwnerMessageVH(new View(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SelfMessageVH) {
            ((SelfMessageVH) holder).bindData(getItem(position), mRetryClickListener);
        } else if (holder instanceof ContactMessageVH) {
            ((ContactMessageVH) holder).bindData(getItem(position), mAvatarClickListener);
        }
    }

    @Override
    public int getItemCount() {
        synchronized (mLock) {
            return mItems.size();
        }
    }

    public ChatMessage getItem(int position) {
        if (position < 0 || position >= getItemCount()) {
            return null;
        }

        synchronized (mLock) {
            return mItems.get(position);
        }
    }

    public void setItems(List<ChatMessage> items) {
        synchronized (mLock) {
            mItems.clear();
            if (!CollectionUtils.isEmpty(items)) {
                mItems.addAll(items);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = getItem(position);

        if (chatMessage != null && chatMessage.getSenderId() == mUser.getUserId()) {
            return VIEW_TYPE_SELF_MESSAGE;
        }

        if (chatMessage != null && chatMessage.getSenderId() == mCurrentContact.getUserId()) {
            return VIEW_TYPE_CONTACT_MESSAGE;
        }

        return VIEW_TYPE_UNKNOWN_OWNER_MESSAGE;
    }

    public void setCurrentContact(Profile currentContact) {
        mCurrentContact = currentContact;
    }

    private boolean isSelfMessage(int viewType) {
        return viewType == VIEW_TYPE_SELF_MESSAGE;
    }

    private boolean isContactMessage(int viewType) {
        return viewType == VIEW_TYPE_CONTACT_MESSAGE;
    }

    public void updateMessage(@ChatMessage.ChatMessageState int state, int requestId, ZLive.ZAPIPrivateChatItem chatItem) {
        int changedPosition = -1;
        synchronized (mLock) {
            int size = mItems.size();
            for (int i = 0; i < size; i++) {
                ChatMessage chatMessage = mItems.get(i);

                if (chatMessage.getRequestId() == requestId) {
                    changedPosition = i;
                    chatMessage.setState(state);
                    chatMessage.setChatItem(chatItem);
                }
            }
        }

        if (changedPosition > -1) {
            notifyItemChanged(changedPosition);
        }
    }

    public void binaryAdd(ChatMessage chatMessage) {
        synchronized (mLock) {
            mItems.add(chatMessage);
        }
    }

    public void setAvatarClickListener(OnAvatarClickListener avatarClickListener) {
        mAvatarClickListener = avatarClickListener;
    }

    public void setRetryClickListener(OnRetryClickListener retryClickListener) {
        mRetryClickListener = retryClickListener;
    }

    public void insert(List<ChatMessage> offlineMessages) {
        if (offlineMessages == null) {
            return;
        }

        synchronized (mLock) {
            mItems.addAll(offlineMessages);
        }
    }

    public void setAvatarLoader(AvatarLoader avatarLoader) {
        mAvatarLoader = avatarLoader;
    }

    private static final class PrivateChatSortedListCallback extends SortedListAdapterCallback<ChatMessage> {

        /**
         * Creates a {@link SortedList.Callback} that will forward data change events to the provided
         * Adapter.
         *
         * @param adapter The Adapter instance which should receive events from the SortedList.
         */
        public PrivateChatSortedListCallback(RecyclerView.Adapter adapter) {
            super(adapter);
        }

        @Override
        public int compare(ChatMessage o1, ChatMessage o2) {
            return Long.signum(o1.getCreatedTime() - o2.getCreatedTime());
        }

        @Override
        public boolean areContentsTheSame(ChatMessage oldItem, ChatMessage newItem) {
            if (!TextUtils.equals(oldItem.getMessage(), newItem.getMessage())) {
                return false;
            }
            if (oldItem.getCreatedTime() != newItem.getCreatedTime()) {
                return false;
            }
            return true;
        }

        @Override
        public boolean areItemsTheSame(ChatMessage item1, ChatMessage item2) {
            return item1.getId() == item2.getId();
        }
    }

    /**
     * {@link PrivateChatMessageVH}
     */
    protected abstract static class PrivateChatMessageVH extends RecyclerView.ViewHolder {

        @BindView(R.id.message_text)
        TextView mMessageText;

        @BindView(R.id.message_created_time)
        TextView mMessageCreatedTime;

        public PrivateChatMessageVH(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        protected void bindData(ChatMessage chatMessage) {
            setContent(chatMessage);
        }

        private void setContent(ChatMessage chatMessage) {
            Context context = itemView.getContext();

            mMessageText.setText(chatMessage.getMessage());

            mMessageCreatedTime.setText(DateUtil.getChatDisplayTime(chatMessage.getCreatedTime(), context));
        }
    }


    /**
     * {@link SelfMessageVH}
     */
    protected static class SelfMessageVH extends PrivateChatMessageVH {

        @BindView(R.id.btn_retry)
        View mRetryButton;

        @BindView(R.id.alert_send_fail)
        View mAlertSendFailView;

        public SelfMessageVH(View itemView) {
            super(itemView);
        }

        protected void bindData(final ChatMessage chatMessage, final OnRetryClickListener listener) {
            super.bindData(chatMessage);

            if (chatMessage.getState() == ChatMessage.STATE_SEND_ERROR) {
                showFailedAlert();

                mRetryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            hideFailedAlert();

                            chatMessage.setState(ChatMessage.STATE_NEWLY_CREATE);
                            listener.onRetryClick(chatMessage);
                        }
                    }
                });
            }
        }

        private void showFailedAlert() {
            mMessageCreatedTime.setVisibility(View.GONE);
            mAlertSendFailView.setVisibility(View.VISIBLE);
            mRetryButton.setVisibility(View.VISIBLE);
        }

        private void hideFailedAlert() {
            mMessageCreatedTime.setVisibility(View.VISIBLE);
            mAlertSendFailView.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.GONE);
        }
    }

    /**
     * {@link ContactMessageVH}
     */
    protected static class ContactMessageVH extends PrivateChatMessageVH {

        @BindView(R.id.avatar)
        CircleImageView mAvatar;

        private AvatarLoader mAvatarLoader;

        public ContactMessageVH(View itemView, AvatarLoader avatarLoader) {
            super(itemView);

            mAvatarLoader = avatarLoader;
        }

        protected void bindData(ChatMessage chatMessage, final OnAvatarClickListener listener) {
            super.bindData(chatMessage);

            if (mAvatarLoader != null) {
                mAvatarLoader.loadAvatar(mAvatar);
            }

            mAvatar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAvatarClick();
                }
            });
        }
    }

    /**
     * {@link UnknownOwnerMessageVH}
     */
    protected static class UnknownOwnerMessageVH extends RecyclerView.ViewHolder {

        public UnknownOwnerMessageVH(View itemView) {
            super(itemView);
        }
    }

    /**
     * {@link OnAvatarClickListener}
     */
    public interface OnAvatarClickListener {
        void onAvatarClick();
    }

    /**
     * {@link OnRetryClickListener}
     */
    public interface OnRetryClickListener {
        void onRetryClick(ChatMessage chatMessage);
    }

    /**
     * {@link AvatarLoader}
     */
    public interface AvatarLoader {
        void loadAvatar(ImageView imageView);
    }
}
