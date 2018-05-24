package com.vng.datasync.data;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 03/05/2018
 */

public final class Transformers {
    private Transformers() {}

    public static final Transformer<Integer, Integer> ZERO_IF_NULL = input ->
            input == null ? 0 : input;
}
