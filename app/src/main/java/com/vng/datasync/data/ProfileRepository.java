package com.vng.datasync.data;

import com.vng.datasync.data.model.Profile;

import rx.Observable;

/**
 * @author thuannv
 * @since 24/05/2018
 */
public final class ProfileRepository {

    private static volatile ProfileRepository sInstance = null;

    private final DataProvider mDataProvider;

    private ProfileRepository() {
        mDataProvider = DataProvider.getInstance();
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

    public Observable<Profile> get(int profileId) {
        return Observable.defer(() -> Observable.just(mDataProvider.getProfile(profileId)));
    }
}
