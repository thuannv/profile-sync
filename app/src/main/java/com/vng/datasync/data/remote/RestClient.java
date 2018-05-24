package com.vng.datasync.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vng.datasync.BuildConfig;
import com.vng.datasync.util.AndroidUtilities;
import com.vng.datasync.util.Logger;

import org.apache.http.conn.ssl.StrictHostnameVerifier;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author thuannv
 * @since 18/07/2017
 */
public class RestClient {
    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(RestClient.class, BuildConfig.DEBUG && DEBUG);

    private static final long CACHE_SIZE = 10 * 1024 * 1024;

    private Retrofit mRetrofit;

    private static volatile RestClient sInstance = null;

    private RestClient(Context context) {
        final Cache cache = createCache(context, CACHE_SIZE);

        final Interceptor requestInterceptor = new DefaultParamsInterceptor(
                AndroidUtilities.getDeviceId(),
                AndroidUtilities.getDeviceName(),
                BuildConfig.VERSION_NAME);

        final HttpLoggingInterceptor httpLoggingInterceptor = defaultLoggingInterceptor();

        /**
         * Issue #195 - Remove pinned certificate
         *
         * As a result of certificate expiration causes client application failed to request
         * API from server. Therefore, we use default device trust with bundled CAs instead of
         * using pinning certificate. This might introduce security hole for man-in-the-middle
         * attach, we should investigate further on this issue in the near future.
         */
//        SSLContext sslContext = null;
//        try {
//            sslContext = SslUtils.getSslConfig(context, R.raw.star_360live_vn_plus);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        OkHttpClient client = createClient(cache, /*sslContext*/ null, requestInterceptor, httpLoggingInterceptor, 60, 60, 60);

        final GsonBuilder gsonBuilder = new GsonBuilder();

        final String url = "https://api.360live.vn/";

        mRetrofit = createRetrofit(url, client, gsonBuilder.create(), RxJavaCallAdapterFactory.create());
    }

    public static RestClient getInstance(Context context) {
        RestClient localInstance = sInstance;
        if (localInstance == null) {
            synchronized (RestClient.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    localInstance = sInstance = new RestClient(context);
                }
            }
        }
        return localInstance;
    }

    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

    public static void cleanUp() {
        sInstance = null;
    }

    private Cache createCache(Context context, long size) {
        return new Cache(context.getCacheDir(), size);
    }

    private HttpLoggingInterceptor defaultLoggingInterceptor() {
        HttpLoggingInterceptor.Logger logger = new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if (BuildConfig.DEBUG) {
                    L.i(message.replaceAll("%", "%%"));
                }
            }
        };
        HttpLoggingInterceptor.Level level = BuildConfig.DEBUG ?
                HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE;

        return createLoggingInterceptor(logger, level);
    }

    private HttpLoggingInterceptor createLoggingInterceptor(HttpLoggingInterceptor.Logger logger,
                                                            HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(logger);
        httpLoggingInterceptor.setLevel(level);
        return httpLoggingInterceptor;
    }

    private OkHttpClient createClient(Cache cache,
                                      SSLContext sslContext,
                                      Interceptor requestInterceptor,
                                      HttpLoggingInterceptor loggingInterceptor,
                                      long readTimeOut /* seconds */,
                                      long writeTimeout /* seconds */,
                                      long connectionTimeout /* seconds */) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (cache != null) {
            builder.cache(cache);
        }
        if (requestInterceptor != null) {
            builder.addInterceptor(requestInterceptor);
        }
        if (loggingInterceptor != null) {
            builder.addInterceptor(loggingInterceptor);
        }
        if (sslContext != null) {
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.hostnameVerifier(new StrictHostnameVerifier());
        }
        builder.readTimeout(readTimeOut, TimeUnit.SECONDS);
        builder.writeTimeout(writeTimeout, TimeUnit.SECONDS);
        builder.connectTimeout(connectionTimeout, TimeUnit.SECONDS);
        return builder.build();
    }

    private Retrofit createRetrofit(String baseUrl,
                                    OkHttpClient client,
                                    Gson gson,
                                    CallAdapter.Factory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }
}