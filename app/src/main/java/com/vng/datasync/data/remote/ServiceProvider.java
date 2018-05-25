package com.vng.datasync.data.remote;

import com.vng.datasync.Injector;
import com.vng.datasync.data.remote.rest.api.UserService;

/**
 * Copyright (C) 2017, VNG Corporation.
 * @author thuannv
 * @since  8/16/2017.
 */
public final class ServiceProvider {

    private static UserService sUserService;

    private ServiceProvider() {
    }

    public static synchronized UserService getUserService() {
        UserService userService = sUserService;
        if (userService == null) {
            synchronized (ServiceProvider.class) {
                userService = sUserService;
                if (userService == null) {
                    userService = sUserService = Injector.providesUserService();
                }
            }
        }
        return userService;
    }
}
