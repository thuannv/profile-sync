package com.vng.datasync.data.local.room;

import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.model.roomdb.ChatProfileDBO;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 27/04/2018
 */
public class ProfileRepository {
    private RoomDatabaseManager mRoomDatabaseManager;

    private static volatile ProfileRepository sInstance = null;

    private ProfileRepository() {
        mRoomDatabaseManager = RoomDatabaseManager.getInstance();
    }

    public static ProfileRepository getInstance() {
        ProfileRepository instance = sInstance;
        if (instance == null) {
            synchronized (ProfileRepository.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new ProfileRepository();
                }
            }
        }
        return instance;
    }

    public List<Profile> getAllProfiles() {
        List<Profile> ret = new ArrayList<>();

        List<ChatProfileDBO> dboList = mRoomDatabaseManager.getProfiles();
        for (ChatProfileDBO dbo : dboList) {
            ret.add(Mappers.FROM_DBO_TO_PROFILE.map(dbo));
        }

        return ret;
    }

    public boolean save(Profile profile) {
        ChatProfileDBO dbo = Mappers.FROM_PROFILE_TO_DBO.map(profile);
        return mRoomDatabaseManager.save(dbo);
    }
}
