package com.vng.datasync.data.local.room;

import com.vng.datasync.data.model.roomdb.ChatConversationDBO;
import com.vng.datasync.data.model.roomdb.ChatMessageDBO;

import java.util.Comparator;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 09/05/2018
 */

public final class Comparators {
    private Comparators() {

    }

    public static final Comparator<ChatMessageDBO> CHAT_MESSAGE_DBO_COMPARATOR = (left, right) -> {
        if (left == null && right == null) {
            return 0;
        }

        if (left == null) {
            return -1;
        }

        if (right == null) {
            return 1;
        }

        return Long.signum(left.id - right.id);
    };

    public static final Comparator<ChatConversationDBO> CHAT_CONVERSATION_DBO_COMPARATOR = (left, right) -> {
        if (left == null && right == null) {
            return 0;
        }

        if (left == null) {
            return -1;
        }

        if (right == null) {
            return 1;
        }

        return Long.signum(left.roomId - right.roomId);
    };
}
