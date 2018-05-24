package com.vng.datasync.data;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 27/10/2017
 */

public interface Transformer<T, R> {
    R transform(T from);
}
