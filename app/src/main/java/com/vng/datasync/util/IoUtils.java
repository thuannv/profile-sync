package com.vng.datasync.util;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 11/08/2017
 */
public final class IoUtils {

    private IoUtils() {
        throw new IllegalStateException("Cannot instantiate object of utility class");
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // ignored
            }
        }
    }

    public static String readAssetFile(AssetManager assets, String file) {
        String content = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(assets.open(file)));
            String line;
            StringBuilder sb = new StringBuilder();
            while ( (line = reader.readLine()) != null) {
                sb.append(line);
            }
            content = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            safeClose(reader);
        }
        return content;
    }
}