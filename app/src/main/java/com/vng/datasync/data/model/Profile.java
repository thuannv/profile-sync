package com.vng.datasync.data.model;

import android.support.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.vng.datasync.data.model.Profile.State.PROFILE_STATE_EMPTY;
import static com.vng.datasync.data.model.Profile.State.PROFILE_STATE_SYNCED;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 08/08/2017
 */

public class Profile {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PROFILE_STATE_EMPTY, PROFILE_STATE_SYNCED})
    public @interface State {
        int PROFILE_STATE_EMPTY = 0;
        int PROFILE_STATE_SYNCED = PROFILE_STATE_EMPTY + 1;
    }

    @SerializedName("avatar")
    private String mAvatar;

    @SerializedName("displayName")
    private String mDisplayName;

    @SerializedName("userId")
    private int mUserId;

    private transient @State int mState;

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }

    public @State int getState() {
        return mState;
    }

    public void setState(@State int state) {
        mState = state;
    }

    public boolean isEmpty() {
        return mState == PROFILE_STATE_EMPTY;
    }

    public boolean isSynced() {
        return mState == PROFILE_STATE_SYNCED;
    }
}
