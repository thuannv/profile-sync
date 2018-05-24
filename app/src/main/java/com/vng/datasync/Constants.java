package com.vng.datasync;

/**
 * @author thuannv
 * @since 19/07/2017
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Not allow instantiating object.");
    }

    public static final long PROFILE_FETCHING_INTERVAL = 6 * 60 * 60 * 1000L * 0; //Millis second

    public static final long FRIEND_REQUESTS_GETTING_INTERVAL = 30 * 60 * 1000; // 30 min interval

    public static final int SWITCH_VARIANT_HIT_COUNT = 10;

    public static final int ANDROID_PLATFORM = 1;

    public static final long OFFLINE_MSG_SYNC_INTERVAL = /*24 * 60 * 10 * */1000L;

    public static final int TOP_RANK = 0;

    public static final long GIFT_INFO_UPDATE_INTERVAL = 6 * 60 * 60 * 1000L;

    public static final int DIAMOND_COUNT_NEED_FOR_FLYING = 10;

    public static final long REMIND_UPDATE_APP_INTERVAL = 2 * 24 * 60 * 60 * 1000L;

    public static final String PLAY_STORE_FORMAT = "market://details?id=%s";

    public static final String PLAY_STORE_WEB_LINK = "https://play.google.com/store/apps/details?id=%s";

    public static final String DOMAIN_DEFAULT = ".360live.vn";

    public static final String LIVE_STREAM_STAT_URL = "https://360live.vn/webview/profile";
}
