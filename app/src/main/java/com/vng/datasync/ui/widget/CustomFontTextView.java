package com.vng.datasync.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.vng.datasync.R;
import com.vng.datasync.util.TypeFaceUtils;

import static com.vng.datasync.util.TypeFaceUtils.applyCustomFont;

/**
 * @author thuannv
 * @since 25/07/2017
 */
public class CustomFontTextView extends AppCompatTextView {

    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(this, context, attrs, R.styleable.CustomFontTextView, R.styleable.CustomFontTextView_fontPath);
    }

    public CustomFontTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(this, context, attrs, R.styleable.CustomFontTextView, R.styleable.CustomFontTextView_fontPath);
    }

    public void setFont(String fontPath) {
        if (!TextUtils.isEmpty(fontPath)) {
            Typeface typeface = TypeFaceUtils.load(getContext().getAssets(), fontPath);
            if (typeface != null) {
                setTypeface(typeface);
                setIncludeFontPadding(false);
            }
        }
    }
}
