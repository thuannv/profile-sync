package com.vng.datasync.data.event;

/**
 * @author thuannv
 * @since 17/07/2017
 */
public final class Event {

    private static final int EVENT_BASE = 0;

    public static final int PROFILE_SYNC_SUCCESS = EVENT_BASE + 1;
    public static final int PROFILE_SYNC_FAILURE = EVENT_BASE + 2;

    public static final int TOKEN_EXPIRED = EVENT_BASE + 3;

    public static final int NETWORK_CONNECTION_STATE_CHANGES = EVENT_BASE + 4;

    public static final int NEW_FRIEND_REQUEST = EVENT_BASE + 5;
    public static final int NEW_PRIVATE_CHAT_MESSAGE_EVENT = EVENT_BASE + 6;
    public static final int SEND_CHAT_RESULT_EVENT = EVENT_BASE + 7;
    public static final int BLOCKED_PRIVATE_CHAT_EVENT = EVENT_BASE + 8;
    public static final int DELETED_CONVERSATION = EVENT_BASE + 9;
    public static final int SYNC_OFFLINE_MESSAGES_COMPLETED = EVENT_BASE + 10;
    public static final int SYNC_OFFLINE_CHANNEL_COMPLETE = EVENT_BASE + 11;

    public static final int HIDE_FLOATING_CHAT = EVENT_BASE + 12;

    public static final int REMINDER_ON = EVENT_BASE + 12;
    public static final int REMINDER_OFF = EVENT_BASE + 13;

    public static final int WEBSOCKET_CONNECTED = EVENT_BASE + 14;
    public static final int WEBSOCKET_HANDSHAKE_REFUSES = EVENT_BASE + 15;
    public static final int WEBSOCKET_DISCONNECTED = EVENT_BASE + 16;

    public static final int REMOVE_LRU_GIFT_EVENT = EVENT_BASE + 17;
    public static final int REFRESH_DISCOVER_FEEDS_EVENT = EVENT_BASE + 18;
    public static final int SET_COMPLETED_MISSIONS_COUNT_EVENT = EVENT_BASE + 19;
    public static final int SHOW_SHARE_LAYOUT_EVENT = EVENT_BASE + 20;

    public static final int UPDATE_CANDY_POINT_EVENT = EVENT_BASE + 21;

    public static final int UPDATE_EQUIPMENT_DETAIL_DIALOG_EVENT = EVENT_BASE + 22;

    public static final int DISMISS_LIVE_USER_POPUP = EVENT_BASE + 23;

    private Event() {
    }
}
