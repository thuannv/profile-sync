package com.vng.datasync.data.local;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 22/05/2018
 */

public class UserManager {
    private static User mUser;

    public static synchronized User getCurrentUser() {
        if (mUser == null) {
            mUser = new User();
            mUser.setAccessToken("");
        }

        return mUser;
    }
}
