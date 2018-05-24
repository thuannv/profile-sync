package com.vng.datasync.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.vng.datasync.data.model.Profile;
import com.vng.datasync.util.CollectionUtils;
import com.vng.datasync.util.IoUtils;
import com.vng.datasync.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author thuannv
 * @since 24/05/2018
 */
public final class DataProvider {

    private static final Logger L = Logger.getLogger(DataProvider.class, true);

    private static volatile DataProvider sInstance = null;

    private volatile boolean mIsInitializing = false;

    private volatile boolean mIsInitialized = false;

    private Map<Integer, Profile> mIdProfilesMap;

    private List<Profile> mProfiles;

    private final CountDownLatch mInitLatch = new CountDownLatch(1);

    private final Random mRandom = new Random();

    private DataProvider() {
    }

    public static DataProvider getInstance() {
        DataProvider instance = sInstance;
        if (instance == null) {
            synchronized (DataProvider.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new DataProvider();
                }
            }
        }
        return instance;
    }

    public void init(@NonNull Context context) {
        if (mIsInitialized) {
            L.w("DataProvider is already initialized.");
            return ;
        }
        if (mIsInitializing) {
            L.w("DataProvider is initializing...");
            return ;
        }
        L.d("Start initializing DataProvider...");
        mIsInitializing = true;
        asyncInitialize(context);
    }

    @SuppressLint("UseSparseArrays")
    private void syncInit(@NonNull Context context) {
        final String json = IoUtils.readAssetFile(context.getAssets(), "data.json");
        final JsonListProfileTransformer transformer = new JsonListProfileTransformer();
        final List<Profile> profiles = transformer.transform(json);
        mProfiles = profiles;
        mIdProfilesMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(profiles)) {
            L.d("Number of profiles %d", profiles.size());
            for (Profile profile : profiles) {
                mIdProfilesMap.put(profile.getUserId(), profile);
            }
        }
    }

    private void asyncInitialize(@NonNull Context context) {
        new Thread(() -> {
            try {
                syncInit(context);
                mIsInitialized = true;
            } catch (Exception e) {
                e.printStackTrace();
                mIsInitialized = false;
            } finally {
                mIsInitializing = false;
                mInitLatch.countDown();
            }
        }).start();
    }

    public Profile getProfile(int profileId) {
        L.d("getProfile() for id=%d on thread=%s", profileId, Thread.currentThread().getName());
        Profile profile = null;
        try {
            mInitLatch.await();
            profile = mIdProfilesMap.get(profileId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return profile;
    }

    public Profile random() {
        try {
            mInitLatch.await();
            int size = mProfiles.size();
            int i = mRandom.nextInt(size);
            return mProfiles.get(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
