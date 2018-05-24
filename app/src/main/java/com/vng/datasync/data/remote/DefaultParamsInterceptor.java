package com.vng.datasync.data.remote;

import com.vng.datasync.util.PlatformUtils;

import java.io.IOException;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author thuannv
 * @since 19/07/2017
 */
public class DefaultParamsInterceptor implements Interceptor {

    private final String mDeviceId;

    private final String mDeviceName;

    private final String mAppVersion;

    private final String mPlatformId = String.valueOf(PlatformUtils.ANDROID_PLATFORM_ID);

    public DefaultParamsInterceptor(String deviceId,
                                    String deviceName,
                                    String appVersion) {
        mDeviceId = deviceId;
        mDeviceName = deviceName;
        mAppVersion = appVersion;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request origin = chain.request();

        Request request = origin.newBuilder()
                .addHeader("locale", Locale.getDefault().getLanguage())
                .build();

        HttpUrl url = request.url().newBuilder()
                .addQueryParameter("devid", mDeviceId)
                .addQueryParameter("appversion", mAppVersion)
                .addQueryParameter("device_name", mDeviceName)
                .addQueryParameter("platform", mPlatformId)
                .build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    }
}
