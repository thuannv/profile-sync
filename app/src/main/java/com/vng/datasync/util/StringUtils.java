package com.vng.datasync.util;

import android.text.TextUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.Locale;

/**
 * @author thuannv
 * @since 18/08/2017
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException();
    }

    private static final char[] SUFFIXES = new char[]{'K', 'M', 'G', 'T', 'P', 'E'};

    public static String withSuffix(long value) {
        try {
            if (value < 1000) {
                return "" + value;
            }
            int exp = (int) (Math.log(value) / Math.log(1000));
            return format("%.1f%c", value / Math.pow(1000, exp), SUFFIXES[exp - 1]);
        } catch (Exception e) {
            return "";
        }
    }

    public static String withSuffixX10(long value) {
        try {
            if (value < 10000) return "" + value;
            int exp = (int) (Math.log(value) / Math.log(1000));
            return format("%.1f%c", value / Math.pow(1000, exp), SUFFIXES[exp - 1]);
        } catch (Exception e) {
            return "";
        }
    }

    public static String format(String fmt, Object... args) {
        return String.format(Locale.US, fmt, args);
    }

    public static String formatConversationUnreadCount(int unreadCount) {
        return String.valueOf(Math.min(unreadCount, 999));
    }

    public static String formatGiftFreeCount(int count) {
        String countStr = String.valueOf(count);
        if (count >= 100) {
            countStr = "99+";
        }
        return countStr;
    }

    public static String normalizeSearchKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return key;
        }

        key = key.trim();
        return Normalizer.normalize(key, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("đ", "d");
    }

    public static String formatFloatingCounter(int value) {
        value = Math.max(0, value); // eliminate negative value
        return value < 100 ? String.valueOf(value) : "99+";
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) {
            return "";
        }
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
