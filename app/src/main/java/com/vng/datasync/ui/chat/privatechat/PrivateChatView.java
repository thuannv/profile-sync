package com.vng.datasync.ui.chat.privatechat;

import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 04/05/2018
 */

public interface PrivateChatView<M> {
    void setChatMessages(List<M> chatMessages);
}
