package com.vng.datasync.data.local;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 22/05/2018
 */

public class User {
    private int mUserId;

    private String mAccessToken;

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }
}
