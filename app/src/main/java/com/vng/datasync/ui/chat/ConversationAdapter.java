package com.vng.datasync.ui.chat;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vng.datasync.GlideApp;
import com.vng.datasync.R;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.ui.widget.CircleImageView;
import com.vng.datasync.ui.widget.CustomFontTextView;
import com.vng.datasync.util.CollectionUtils;
import com.vng.datasync.util.ProfileManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;
import static com.vng.datasync.util.DateUtil.getChatDisplayTime;
import static com.vng.datasync.util.StringUtils.formatConversationUnreadCount;

/**
 * @author namnt4
 * @since 21/08/2017
 */

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Object LOCK = new Object();

    private final int VIEW_TYPE_BASE_ITEM = 0;

    private final int AVATAR_SIZE;

    @ColorInt
    private final int TEXT_COLOR_TITLE_REGULAR;

    @ColorInt
    private final int TEXT_COLOR_TITLE_BOLD;

    @ColorInt
    private final int TEXT_COLOR_SUBTITLE_REGULAR;

    @ColorInt
    private final int TEXT_COLOR_SUBTITLE_BOLD;

    private final String FONT_SEMI_BOLD;

    private final String FONT_REGULAR;

    private List<ChatConversation> mItems;

    private ConversationClickHandler mConversationClickHandler;

    public ConversationAdapter(Context context) {
        mItems = new ArrayList<>();

        AVATAR_SIZE = context.getResources().getDimensionPixelSize(R.dimen.conversation_avatar_size);

        TEXT_COLOR_TITLE_REGULAR = ContextCompat.getColor(context, R.color.color_4d4d4d);
        TEXT_COLOR_TITLE_BOLD = ContextCompat.getColor(context, R.color.color_262626);
        TEXT_COLOR_SUBTITLE_REGULAR = ContextCompat.getColor(context, R.color.read_conversation_subtitle);
        TEXT_COLOR_SUBTITLE_BOLD = ContextCompat.getColor(context, R.color.color_262626);

        FONT_SEMI_BOLD = context.getString(R.string.SFUIText_Semibold);
        FONT_REGULAR = context.getString(R.string.SFUIText_Regular);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_home_item_new, parent, false);

        switch (viewType) {
            case VIEW_TYPE_BASE_ITEM:
                return new ConversationVH(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ConversationVH) {
            ((ConversationVH) holder).bindData(mItems.get(position));

        }
    }

    public void setData(List<ChatConversation> items) {
        synchronized (LOCK) {
            mItems.clear();
            if (!CollectionUtils.isEmpty(items)) {
                mItems.addAll(items);
            }
        }

        notifyDataSetChanged();
    }

    public ChatConversation getItem(int index) {
        if (index < 0 || index > getItemCount()) {
            return null;
        }

        ChatConversation item;

        synchronized (LOCK) {
            item = mItems.get(index);
        }

        return item;
    }

    public int indexOf(long roomId) {
        synchronized (LOCK) {
            int size = mItems.size();
            for (int i = 0; i < size; i++) {
                ChatConversation chatConversation = mItems.get(i);
                if (chatConversation.getId() == roomId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfProfileId(int profileId) {
        synchronized (LOCK) {
            int size = mItems.size();
            for (int i = 0; i < size; i++) {
                ChatConversation chatConversation = mItems.get(i);
                if (chatConversation.getContactId() == profileId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void updateProfileSyncStatus(int profileId) {
        int indexOfProfileId = indexOfProfileId(profileId);
        notifyItemChanged(indexOfProfileId);
    }

    public void removeItem(int index) {
        if (index < 0 || index >= getItemCount()) {
            return;
        }

        synchronized (LOCK) {
            mItems.remove(index);
        }

        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        synchronized (LOCK) {
            return mItems.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_BASE_ITEM;
    }

    public void setConversationClickHandler(ConversationClickHandler listener) {
        mConversationClickHandler = listener;
    }

    /**
     * {@link ConversationVH}
     */
    protected class ConversationVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.avatar)
        CircleImageView mAvatar;

        @BindView(R.id.loading_avatar)
        ProgressBar mLoadingAvatar;

        @BindView(R.id.title)
        CustomFontTextView mTitle;

        @BindView(R.id.subtitle)
        CustomFontTextView mSubtitle;

        @BindView(R.id.last_message_time)
        CustomFontTextView mLastMessageTime;

        @BindView(R.id.message_count)
        TextView mMessageCount;

        private final Context mContext;

        private final String TEXT_LOADING;

        private final ProfileManager mProfileManager;

        private ConversationVH(View itemView) {
            super(itemView);

            mContext = itemView.getContext();

            ButterKnife.bind(this, itemView);

            mProfileManager = ProfileManager.getInstance();

            Context context = itemView.getContext();

            TEXT_LOADING = context.getString(R.string.profile_progress);

            initViews();
        }

        private void initViews() {
            itemView.setOnClickListener(this);
        }

        private void bindData(ChatConversation data) {
            setContent(data);

            checkShowStatus(data);

            boolean shouldHighlight = data.getUnreadCount() > 0;
            setFontAndTextColor(shouldHighlight);
        }

        private void checkShowStatus(ChatConversation data) {
            int unreadCount = data.getUnreadCount();

            mMessageCount.setVisibility(unreadCount > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        private void setContent(ChatConversation data) {
            int friendId = data.getContactId();

            Profile profile = mProfileManager.get(friendId);

            setAvatarAndTitle(mContext, profile);

            mSubtitle.setText(data.getSnippet());

            mLastMessageTime.setText(getChatDisplayTime(data.getLastModifiedTime(), mContext));

            mMessageCount.setText(formatConversationUnreadCount(data.getUnreadCount()));
        }

        protected void setFontAndTextColor(boolean shouldHighlight) {
            String font = shouldHighlight ? FONT_SEMI_BOLD : FONT_REGULAR;

            int titleTextColor = shouldHighlight ? TEXT_COLOR_TITLE_BOLD : TEXT_COLOR_TITLE_REGULAR;

            int subtitleTextColor = shouldHighlight ? TEXT_COLOR_SUBTITLE_BOLD : TEXT_COLOR_SUBTITLE_REGULAR;

            mTitle.setFont(font);
            mTitle.setTextColor(titleTextColor);

            mSubtitle.setFont(font);
            mSubtitle.setTextColor(subtitleTextColor);

            mLastMessageTime.setFont(font);
            mLastMessageTime.setTextColor(subtitleTextColor);
        }

        private void setAvatarAndTitle(Context context, Profile profile) {
            int friendId = profile.getUserId();

            if (profile.isEmpty()) {
                mAvatar.setVisibility(View.VISIBLE);
                mAvatar.setImageResource(R.drawable.avatar_default);

                mLoadingAvatar.setVisibility(View.GONE);

                mTitle.setText(context.getString(R.string.display_user_id, String.valueOf(friendId)));
                ProfileManager.getInstance().sync(friendId);
            }

            if (mProfileManager.isSyncing(friendId)) {
                mAvatar.setVisibility(View.GONE);

                mLoadingAvatar.setVisibility(View.VISIBLE);

                String displayName = profile.getDisplayName();
                mTitle.setText(isEmpty(displayName) ? TEXT_LOADING : displayName);
            }

            if (profile.isSynced()) {
                GlideApp.with(context)
                        .load(profile.getAvatar())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .dontAnimate()
                        .override(AVATAR_SIZE)
                        .placeholder(R.color.silver)
                        .error(R.drawable.avatar_default)
                        .into(mAvatar);

                mLoadingAvatar.setVisibility(View.GONE);

                mTitle.setText(profile.getDisplayName());
            }
        }

        @Override
        public void onClick(View v) {
            if (mConversationClickHandler != null) {
                mConversationClickHandler.onConversationClick(mItems.get(getAdapterPosition()));
            }
        }
    }

    /**
     * {@link ConversationClickHandler}
     */
    public interface ConversationClickHandler {
        void onConversationClick(ChatConversation conversation);
    }
}
