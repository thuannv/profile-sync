package com.vng.datasync;

/**
 * @author thuannv
 * @since 25/05/2018
 */
public class Environment {

    private static final int DEVELOPMENT_MODE = 1;

    private static final int STAGING_MODE = 2;

    private static final int PRODUCTION_MODE = 3;

    private static Environment sCurrentEnvironment;

    private final int mMode;

    static {
        sCurrentEnvironment = new Environment(BuildConfig.ENV_MODE);
    }

    private Environment(int mode) {
        mMode = mode;
    }

    public boolean isDevelopment() {
        return DEVELOPMENT_MODE == mMode;
    }

    public boolean isStaging() {
        return STAGING_MODE == mMode;
    }

    public boolean isProduction() {
        return PRODUCTION_MODE == mMode;
    }


    public static Environment current() {
        return sCurrentEnvironment;
    }
}
