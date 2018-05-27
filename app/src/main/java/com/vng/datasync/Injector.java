package com.vng.datasync;

import android.app.Application;

import com.vng.datasync.data.ProfileRepository;
import com.vng.datasync.data.local.room.RoomDatabaseManager;
import com.vng.datasync.data.remote.FakeUserService;
import com.vng.datasync.data.remote.RestClient;
import com.vng.datasync.data.remote.rest.api.UserService;
import com.vng.datasync.data.remote.websocket.FakeWebSocketManager;
import com.vng.datasync.data.remote.websocket.WebSocketManager;
import com.vng.datasync.data.remote.websocket.WebSocketManagerInf;

/**
 * @author thuannv
 * @since 25/05/2018
 *
 * Simple class for resolving dependencies.
 */
public final class Injector {

    private static Application sApplication;

    private Injector() {}

    static {
        sApplication = DataSyncApp.getInstance();
    }

    public static RestClient providesRestClient() {
        return RestClient.getInstance(sApplication);
    }

    public static ProfileRepository providesProfileRepository() {
        return ProfileRepository.getInstance();
    }

    public static UserService providesUserService() {
        if (Environment.current().isDevelopment()) {
            return new FakeUserService(providesProfileRepository());
        }
        return providesRestClient().create(UserService.class);
    }

    public static WebSocketManagerInf providesWebSocketManager() {
        if (Environment.current().isDevelopment()) {
            return FakeWebSocketManager.getInstance();
        }
        return WebSocketManager.getInstance();
    }

    public static RoomDatabaseManager providesDatabaseManager() {
        return RoomDatabaseManager.getInstance();
    }
}
