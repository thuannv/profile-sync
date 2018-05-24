package com.vng.datasync.data.remote.websocket;

import com.vng.datasync.BuildConfig;
import com.vng.datasync.data.model.ConfigsData;
import com.vng.datasync.util.PlatformUtils;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 10/08/2017
 */

public final class WebSocketConfigs {

    private final int mPingTime;

    private final int mRetryTime;

    private final int mRetryCount;

    public WebSocketConfigs(ConfigsData configs, String authKey) {
        mRetryCount = configs.getRetryCount();
        mRetryTime = configs.getRetryTimeout();
        mPingTime = configs.getPingTime();
    }

    public String wsUrl() {
        return "&ts=" + System.currentTimeMillis() +
                "&platform=" + PlatformUtils.ANDROID_PLATFORM_ID +
                "&appversion=" + BuildConfig.VERSION_NAME;
    }

    public int getPingTime() {
        return mPingTime;
    }

    public int getRetryTime() {
        return mRetryTime;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof WebSocketConfigs) {
            final WebSocketConfigs other = ((WebSocketConfigs) obj);
            return mRetryCount == other.mRetryCount
                    && mPingTime == other.mPingTime
                    && mRetryTime == other.mRetryTime;
        }
        return false;
    }

    public static WebSocketConfigs from(ConfigsData configs, String authKey) {
        return new WebSocketConfigs(configs, authKey);
    }
}
