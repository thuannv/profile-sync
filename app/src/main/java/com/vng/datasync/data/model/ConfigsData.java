package com.vng.datasync.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.util.Logger;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 08/08/2017
 */

public class ConfigsData {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(ConfigsData.class, BuildConfig.DEBUG && DEBUG);

    @SerializedName("retry_count")
    private int mRetryCount;

    @SerializedName("ping_time")
    private int mPingTime;

    @SerializedName("retry_timeout")
    private int mRetryTimeout;

    private static final Gson GSON = new Gson();

    public int getRetryCount() {
        return mRetryCount;
    }

    public void setRetryCount(int retryCount) {
        mRetryCount = retryCount;
    }

    public int getPingTime() {
        return mPingTime;
    }

    public void setPingTime(int pingTime) {
        mPingTime = pingTime;
    }

    public int getRetryTimeout() {
        return mRetryTimeout;
    }

    public void setRetryTimeout(int retryTimeout) {
        mRetryTimeout = retryTimeout;
    }

    private static final ConfigsData sDefaultConfigs;

    static {
        sDefaultConfigs = new ConfigsData();
        sDefaultConfigs.setRetryTimeout(10000);
        sDefaultConfigs.setPingTime(60000);
        sDefaultConfigs.setRetryCount(10);
    }

    public static ConfigsData getDefaultConfigsData() {
        return sDefaultConfigs;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public static ConfigsData fromJson(String json) {
        ConfigsData configs = null;
        try {
            configs = GSON.fromJson(json, ConfigsData.class);
        } catch (Exception e) {
            L.e(e.toString());
        }
        return configs;
    }
}
