package com.vng.datasync.data.remote;

import com.vng.datasync.protobuf.ZLive;

/**
 * Copyright (C) 2017, VNG Coporation.
 *
 * @author namnt4
 * @since 21/09/2017
 */

public interface MessageListener {
    void onMessage(ZLive.ZAPIMessage message);
}
