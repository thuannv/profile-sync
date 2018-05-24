package com.vng.datasync.util;

import android.content.Context;
import android.util.SparseArray;

import com.vng.datasync.BuildConfig;
import com.vng.datasync.data.event.Event;
import com.vng.datasync.data.event.EventDispatcher;
import com.vng.datasync.data.local.room.ProfileRepository;
import com.vng.datasync.data.local.room.RoomDatabaseManager;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.remote.ServiceProvider;
import com.vng.datasync.data.remote.rest.api.UserService;
import com.vng.datasync.data.remote.rest.response.ProfileResponse;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author thuannv
 * @since 21/08/2017
 */
public final class ProfileManager {

    private static final boolean DEBUG = true;
    
    private static final Logger L = Logger.getLogger(ProfileManager.class, BuildConfig.DEBUG && DEBUG);
    
    private static volatile ProfileManager sInstance = null;

    private volatile boolean mInitialized = false;

    private volatile boolean mInitializing = false;

    private final Executor mWorker;

    private ProfileRepository mProfileRepository;

    private final SparseArray<Profile> mCachedProfiles = new SparseArray<>();

    private final Set<Integer> mSyncingProfiles = new TreeSet<>();

    private ProfileManager() {
        mWorker = Executors.newFixedThreadPool(1);
    }

    public static ProfileManager getInstance() {
        ProfileManager instance = sInstance;
        if (instance == null) {
            synchronized (ProfileManager.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new ProfileManager();
                }
            }
        }
        return instance;
    }

    public void init(final Context context) {
        if (mInitialized || mInitializing) {
            L.d( "ProfileManager is initialized or in initializing process.");
            return;
        }

        mInitializing = true;

        RoomDatabaseManager.getInstance().initForUser(context);

        mProfileRepository = ProfileRepository.getInstance();

        mWorker.execute(() -> {
            try {
                cacheProfiles(mProfileRepository.getAllProfiles());
                mInitialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mInitializing = false;
            }
        });
    }

    private void cacheProfiles(List<Profile> profiles) {
        if (CollectionUtils.isEmpty(profiles)) {
            return;
        }
        synchronized (mCachedProfiles) {
            for (Profile p : profiles) {
                mCachedProfiles.put(p.getUserId(), p);
            }
        }
    }

    private boolean isInitialized() {
        if (!mInitialized) {
            L.e( "ProfileManager was not initialized.");
            return false;
        }
        return true;
    }

    public void addToCache(Profile profile) {
        if (profile != null && isInitialized()) {
            synchronized (mCachedProfiles) {
                mCachedProfiles.put(profile.getUserId(), profile);
            }
        }
    }

    public void removeFromCached(Profile profile) {
        if (profile != null && isInitialized()) {
            synchronized (mCachedProfiles) {
                mCachedProfiles.remove(profile.getUserId());
            }
        }
    }

    public void save(Profile profile) {
        if (isInitialized()) {
            if (mProfileRepository.save(profile)) {
                addToCache(profile);
            }
        }
    }

    public Profile get(int profileId) {
        Profile profile = null;
        if (isInitialized()) {
            synchronized (mCachedProfiles) {
                profile = mCachedProfiles.get(profileId);
            }
        }
        if (profile == null) {
            profile = new Profile();
            profile.setUserId(profileId);
            profile.setState(Profile.State.PROFILE_STATE_EMPTY);
        }
        return profile;
    }

    protected void addToSyncing(int profileId) {
        synchronized (mSyncingProfiles) {
            mSyncingProfiles.remove(profileId);
        }
    }

    public boolean isSyncing(int profileId) {
        synchronized (mSyncingProfiles) {
            return mSyncingProfiles.contains(profileId);
        }
    }

    protected void removeFromSyncing(int profileId) {
        synchronized (mSyncingProfiles) {
            mSyncingProfiles.remove(profileId);
        }
    }

    public void sync(int profileId) {
        if (isInitialized()) {
            if (isSyncing(profileId)) {
                L.e( "profileId=%d has already been in syncing process.");
                return;
            }
            mWorker.execute(new ProfileSyncJob(this, profileId));
        }
    }

    /**
     * {@link ProfileSyncJob}
     */
    private static final class ProfileSyncJob implements Runnable {

        private int mProfileId;

        private ProfileManager mProfileManager;

        public ProfileSyncJob(ProfileManager profileManager, int profileId) {
            mProfileId = profileId;
            mProfileManager = profileManager;
        }

        @Override
        public void run() {
                final UserService userService = ServiceProvider.getUserService();
                userService.fetchProfile(mProfileId)
                        .subscribe(new ResponseSubscriber<ProfileResponse>() {
                            @Override
                            public void onNext(ProfileResponse profileResponse) {
                                mProfileManager.removeFromSyncing(mProfileId);
                                super.onNext(profileResponse);
                            }

                            @Override
                            public void onSuccess(ProfileResponse profileResponse) {
                                Profile syncedProfile = profileResponse.getData();
                                syncedProfile.setState(Profile.State.PROFILE_STATE_SYNCED);
                                mProfileManager.addToCache(syncedProfile);
                                mProfileManager.save(syncedProfile);
                                EventDispatcher.getInstance().post(Event.PROFILE_SYNC_SUCCESS, mProfileId);
                            }

                            @Override
                            public void onFailure(ProfileResponse profileResponse) {
                                super.onFailure(profileResponse);
                                EventDispatcher.getInstance().post(Event.PROFILE_SYNC_FAILURE, mProfileId);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mProfileManager.removeFromSyncing(mProfileId);
                                EventDispatcher.getInstance().post(Event.PROFILE_SYNC_FAILURE, mProfileId);
                            }
                        });
        }
    }
}
