package com.vng.datasync.data.remote;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 07/09/2017
 */

public interface DataListener {
    void onReceive(int commandId, int subCommandId, Object data);
}
