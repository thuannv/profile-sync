package com.vng.datasync.data.remote;

import com.vng.datasync.data.ProfileRepository;
import com.vng.datasync.data.Transformer;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.remote.rest.api.UserService;
import com.vng.datasync.data.remote.rest.response.ProfileResponse;

import rx.Observable;

/**
 * @author thuannv
 * @since 25/05/2018
 */
public final class FakeUserService implements UserService {

    private static final int SUCCESS_RESPONSE_CODE = 0;

    private final ProfileRepository mRepository;

    public FakeUserService(ProfileRepository repository) {
        mRepository = repository;
    }

    private final Transformer<Profile, Observable<ProfileResponse>> PROFILE_RESPONSE_TRANSFORMER = profile -> {
        ProfileResponse response = new ProfileResponse();
        response.setCode(SUCCESS_RESPONSE_CODE);
        response.setData(profile == null ? Profile.EMPTY : profile);
        return Observable.just(response);
    };

    @Override
    public Observable<ProfileResponse> fetchProfile(int profile) {
        return mRepository.get(profile).flatMap(PROFILE_RESPONSE_TRANSFORMER::transform);
    }
}
