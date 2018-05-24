package com.vng.datasync.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.vng.datasync.DataSyncApp;
import com.vng.datasync.R;
import com.vng.datasync.data.ChatConversation;
import com.vng.datasync.data.ChatMessage;
import com.vng.datasync.data.local.room.RoomDatabaseManager;
import com.vng.datasync.ui.chat.privatechat.contactchat.ContactChatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 18/10/2017
 */

public final class NotificationHelper {

    private static final int LARGE_ICON_SIZE = AndroidUtilities.dp(64);

    private static final int NOTIFICATION_ID_BASE = 122;

    public static final int WATCHING_NOTIFICATION_ID = NOTIFICATION_ID_BASE + 1;

    public static final String PRIVATE_CHAT_CHANNEL_ID = "private_chat_id";

    public static final String GAME_STREAMING_CHANNEL_ID = "game_streaming_id";

    public static final String GENERAL_CHANNEL_ID = "general_id";

    public static final String WATCHING_CHANNEL_ID = "play_stream_local_id";

    private static NotificationManager sNotificationManager;


    private NotificationHelper() {
        throw new UnsupportedOperationException("Not allow instantiating object.");
    }

    static {
        sNotificationManager = (NotificationManager) DataSyncApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        if (PlatformUtils.isAndroidO()) {
            List<NotificationChannel> notificationChannelGroups = new ArrayList<>();
            notificationChannelGroups.add(createNotificationChannel(GENERAL_CHANNEL_ID, DataSyncApp.getInstance().getString(R.string.general_application_notification_visible_name)));
            notificationChannelGroups.add(createNotificationChannel(PRIVATE_CHAT_CHANNEL_ID, DataSyncApp.getInstance().getString(R.string.private_chat_notification_visible_name)));
            notificationChannelGroups.add(createNotificationChannel(WATCHING_CHANNEL_ID, DataSyncApp.getInstance().getString(R.string.watching_livestream_notification_visible_name)));
            notificationChannelGroups.add(createNotificationChannel(GAME_STREAMING_CHANNEL_ID, DataSyncApp.getInstance().getString(R.string.game_streaming_notification_visible_name)));
            sNotificationManager.createNotificationChannels(notificationChannelGroups);
        }
    }

    public static NotificationManager getNotificationManager() {
        return sNotificationManager;
    }

    public static void cancelWatchingStreamNotification() {
        getNotificationManager().cancel(WATCHING_NOTIFICATION_ID);
    }


    public static void cancelAll() {
        getNotificationManager().cancelAll();
    }

    public static void createPrivateChatNotification(String avatar, String title, String content, ChatMessage message) {
        Context context = DataSyncApp.getInstance().getApplicationContext();
        RoomDatabaseManager databaseManager = RoomDatabaseManager.getInstance();

        // Ensure database is initialized
        databaseManager.initForUser(context);

        ChatConversation conversation = databaseManager.getOrCreateContactConversation(message.getSenderId());

        if (conversation == null) {
            return;
        }

        PendingIntent pendingIntent = getPrivateChatPendingIntent(conversation);

        NotificationInfo notificationInfo = new NotificationInfo.Builder()
                .setNotificationId(message.getSenderId())
                .setIntent(pendingIntent)
                .setAvatar(avatar)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_360live)
                .setNotificationCount(conversation.getUnreadCount())
                .setAutoCancel(true)
                .setSound(true)
                .build();

        createNotification(notificationInfo, PRIVATE_CHAT_CHANNEL_ID);
    }

    private static PendingIntent getPrivateChatPendingIntent(ChatConversation conversation) {
        Context context = DataSyncApp.getInstance().getApplicationContext();
        Intent intent = ContactChatActivity.createIntentForActivity(context, conversation);

        if (!DSApplicationLifeCycle.isAppAlive()) {
            intent.setAction(Intent.ACTION_MAIN);
        }

        return PendingIntent.getActivity(context, conversation.getChannelId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void createNotification(NotificationInfo notificationInfo, String notificationChannelId) {
        PendingIntent pendingIntent = notificationInfo.getPendingIntent();
        if (pendingIntent == null) {
            return;
        }

        Context context = DataSyncApp.getInstance().getApplicationContext();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationChannelId);

        Bitmap largeIcon = getLargeIcon(notificationInfo);

        int smallIconRes = PlatformUtils.hasLollipop() ? R.drawable.ic_favicon : notificationInfo.getSmallIcon();

        int notificationCount = notificationInfo.getNotificationCount();

        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(notificationInfo.getContentTitle());
        builder.setContentText(notificationInfo.getContentText());
        builder.setAutoCancel(notificationInfo.isAutoCancel());
        builder.setOngoing(notificationInfo.isOngoing());
        builder.setLargeIcon(largeIcon);
        builder.setSmallIcon(smallIconRes);
        if (notificationInfo.isSound()) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        if (notificationCount > 0) {
            builder.setNumber(notificationCount);
        }
        getNotificationManager().notify(notificationInfo.getNotificationId(), builder.build());
    }

    private static Bitmap getLargeIcon(NotificationInfo info) {
        Bitmap icon = BitmapUtils.decodeSampleBitmapFromUrl(info.getAvatar(), LARGE_ICON_SIZE, LARGE_ICON_SIZE);

        if (icon == null) {
            icon = BitmapUtils.decodeSampledBitmapFromResource(DataSyncApp.getInstance().getResources(), LARGE_ICON_SIZE, LARGE_ICON_SIZE, info.getSmallIcon());
        }

        return icon;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static NotificationChannel createNotificationChannel(String channelId, String userVisibleName) {
        return createNotificationChannel(channelId, userVisibleName, NotificationManager.IMPORTANCE_DEFAULT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static NotificationChannel createNotificationChannel(String channelId, String userVisibleName, int importance) {
        NotificationChannel notificationChannel = new NotificationChannel(channelId, userVisibleName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setSound(null, null);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        return notificationChannel;
    }


    /**
     * {@link NotificationInfo}
     */
    private static final class NotificationInfo {

        private int mStreamId;

        private int mNotificationId;

        private PendingIntent mPendingIntent;

        private int mSmallIcon;

        private String mContentTitle;

        private String mContentText;

        private String mAvatar;

        private int mNotificationCount;

        private boolean mIsAutoCancel = false;

        private boolean mOngoing = false;

        private boolean mIsSound;

        public NotificationInfo(int streamId, int notificationId, PendingIntent pendingIntent, int smallIcon, String contentTitle, String contentText, String avatar, int notificationCount, boolean isAutoCancel, boolean ongoing, boolean isSound) {
            mStreamId = streamId;
            mNotificationId = notificationId;
            mPendingIntent = pendingIntent;
            mSmallIcon = smallIcon;
            mContentTitle = contentTitle;
            mContentText = contentText;
            mAvatar = avatar;
            mNotificationCount = notificationCount;
            mIsAutoCancel = isAutoCancel;
            mOngoing = ongoing;
            mIsSound = isSound;
        }

        public int getStreamId() {
            return mStreamId;
        }

        public int getNotificationId() {
            return mNotificationId;
        }

        public PendingIntent getPendingIntent() {
            return mPendingIntent;
        }

        public int getSmallIcon() {
            return mSmallIcon;
        }

        public String getContentTitle() {
            return mContentTitle;
        }

        public String getContentText() {
            return mContentText;
        }

        public String getAvatar() {
            return mAvatar;
        }

        public int getNotificationCount() {
            return mNotificationCount;
        }

        public boolean isAutoCancel() {
            return mIsAutoCancel;
        }

        public boolean isOngoing() {
            return mOngoing;
        }

        public boolean isSound() {
            return mIsSound;
        }

        public void setSound(boolean sound) {
            mIsSound = sound;
        }

        private static final class Builder {
            private int mStreamId;

            private int mNotificationId;

            private PendingIntent mPendingIntent;

            private int mSmallIcon;

            private String mContentTitle;

            private String mContentText;

            private String mAvatar;

            private int mNotificationCount;

            private boolean mIsAutoCancel;

            private boolean mOngoing;

            private boolean mIsSound = false;

            public Builder() {
            }

            public Builder setStreamId(int streamId) {
                mStreamId = streamId;
                return this;
            }

            public Builder setNotificationId(int notificationId) {
                mNotificationId = notificationId;
                return this;
            }

            public Builder setIntent(PendingIntent intent) {
                mPendingIntent = intent;
                return this;
            }

            public Builder setSmallIcon(int smallIcon) {
                mSmallIcon = smallIcon;
                return this;
            }

            public Builder setContentTitle(String contentTitle) {
                mContentTitle = contentTitle;
                return this;
            }

            public Builder setContentText(String contentText) {
                mContentText = contentText;
                return this;
            }

            public Builder setAvatar(String avatar) {
                mAvatar = avatar;
                return this;
            }

            public Builder setNotificationCount(int notificationCount) {
                mNotificationCount = notificationCount;
                return this;
            }

            public Builder setAutoCancel(boolean autoCancel) {
                mIsAutoCancel = autoCancel;
                return this;
            }

            public Builder setOngoing(boolean ongoing) {
                mOngoing = ongoing;
                return this;
            }

            public Builder setSound(boolean sound) {
                mIsSound = sound;
                return this;
            }

            public NotificationInfo build() {
                return new NotificationInfo(mStreamId, mNotificationId, mPendingIntent, mSmallIcon, mContentTitle, mContentText, mAvatar, mNotificationCount, mIsAutoCancel, mOngoing, mIsSound);
            }
        }
    }
}
