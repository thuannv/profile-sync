package com.vng.datasync.data.remote.rest.api;

import com.vng.datasync.data.remote.rest.response.ProfileResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 08/08/2017
 */

public interface UserService {
    @GET("/profile/{id}")
    Observable<ProfileResponse> fetchProfile(@Path("id") int profile);
}
