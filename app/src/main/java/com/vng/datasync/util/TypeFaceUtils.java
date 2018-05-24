package com.vng.datasync.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author thuannv
 * @since 25/07/2017
 */
public final class TypeFaceUtils {

    private TypeFaceUtils() {}

    private static final Map<String, Typeface> sCachedFonts = new HashMap<>();

    public static Typeface load(AssetManager assets, String fontPath) {
        if (assets == null || TextUtils.isEmpty(fontPath)) {
            return null;
        }
        Typeface typeface = null;
        synchronized (sCachedFonts) {
            typeface = sCachedFonts.get(fontPath);
            if (typeface == null) {
                try {
                    typeface = Typeface.createFromAsset(assets, fontPath);
                    sCachedFonts.put(fontPath, typeface);
                } catch (Exception e) {
                    sCachedFonts.put(fontPath, null);
                    typeface = null;
                }
            }
        }
        return typeface;
    }

    public static void applyCustomFont(View view, Context context, AttributeSet set, int[] attrs, int attributeId) {
        if (view == null || context == null || set == null || attrs == null) {
            return ;
        }

        if (!(view instanceof TextView)) {
            return ;
        }

        String fontPath = null;
        TypedArray a = context.obtainStyledAttributes(set, attrs);
        try {
            fontPath = a.getString(attributeId);
        } finally {
            a.recycle();
        }

        if (!TextUtils.isEmpty(fontPath)) {
            Typeface typeface = TypeFaceUtils.load(context.getAssets(), fontPath);
            if (typeface != null) {
                ((TextView) view).setTypeface(typeface);
            }
        }

        ((TextView) view).setIncludeFontPadding(false);
    }
}
