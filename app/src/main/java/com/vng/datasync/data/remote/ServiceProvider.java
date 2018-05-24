package com.vng.datasync.data.remote;

import com.vng.datasync.DataSyncApp;
import com.vng.datasync.data.remote.rest.api.UserService;

/**
 * Copyright (C) 2017, VNG Corporation.
 * <p>
 * Created by Taindb
 * on 8/16/2017.
 */

public final class ServiceProvider {

    private static RestClient sRestClient;

    private static UserService sUserService;

    private ServiceProvider() {
    }

    public static RestClient getRestClient() {
        RestClient client = sRestClient;
        if (client == null) {
            synchronized (ServiceProvider.class) {
                client = sRestClient;
                if (client == null) {
                    client = sRestClient = RestClient.getInstance(DataSyncApp.getInstance());
                }
            }
        }
        return client;
    }

    public static synchronized UserService getUserService() {
        UserService userService = sUserService;
        if (userService == null) {
            synchronized (ServiceProvider.class) {
                userService = sUserService;
                if (userService == null) {
                    userService = sUserService = getRestClient().create(UserService.class);
                }
            }
        }
        return userService;
    }
}
