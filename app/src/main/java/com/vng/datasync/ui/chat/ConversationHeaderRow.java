package com.vng.datasync.ui.chat;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.vng.datasync.DataSyncApp;
import com.vng.datasync.GlideApp;
import com.vng.datasync.R;
import com.vng.datasync.ui.widget.CircleImageView;
import com.vng.datasync.util.AndroidUtilities;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 03/05/2018
 */

public class ConversationHeaderRow extends ConstraintLayout {

    private CircleImageView mIconView;

    private TextView mTitleView;

    private TextView mBadgeView;

    private final int mIconSize = AndroidUtilities.dp(38);

    public ConversationHeaderRow(Context context) {
        super(context);
        init(context, null);
    }

    public ConversationHeaderRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ConversationHeaderRow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.conversation_header_row, this);

        mIconView = findViewById(R.id.icon);
        mTitleView = findViewById(R.id.title);
        mBadgeView = findViewById(R.id.badge);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ConversationHeaderRow);

        try {
            String title = a.getString(R.styleable.ConversationHeaderRow_conversation_header_title);
            int iconRes = a.getResourceId(R.styleable.ConversationHeaderRow_conversation_header_icon, 0);

            setIcon(iconRes);
            setTitle(title);
        } finally {
            a.recycle();
        }
    }

    public void setIcon(@DrawableRes int res) {
        GlideApp.with(this.getContext())
                .load(res)
                .dontAnimate()
//                .override(mIconSize)
                .into(mIconView);
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setTitle(@StringRes int stringRes) {
        Context context = DataSyncApp.getInstance().getApplicationContext();
        setTitle(context.getString(stringRes));
    }

    public void setBadgeNumber(int count) {
        mBadgeView.setText(String.valueOf(count));
        if (count > 0) {
            mBadgeView.setVisibility(VISIBLE);
        } else {
            mBadgeView.setVisibility(GONE);
        }
    }
}
