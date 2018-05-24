package com.vng.datasync.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.vng.datasync.R;

import static com.vng.datasync.util.TypeFaceUtils.applyCustomFont;

/**
 * @author thuannv
 * @since 25/07/2017
 */
public class CustomFontEditText extends AppCompatEditText {

    public CustomFontEditText(Context context) {
        super(context);
    }

    public CustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(this, context, attrs, R.styleable.CustomFontEditText, R.styleable.CustomFontEditText_fontPath);
    }

    public CustomFontEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(this, context, attrs, R.styleable.CustomFontEditText, R.styleable.CustomFontEditText_fontPath);
    }
}
