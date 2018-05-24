package com.vng.datasync.data.remote.rest.response;

import com.google.gson.annotations.SerializedName;

/**
 * @author thuannv
 * @since 17/07/2017
 */
public class Response<T> {

    @SerializedName("error")
    private int mCode;

    @SerializedName("message")
    private String mMessage;

    @SerializedName("data")
    private T mData;

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public T getData() {
        return mData;
    }

    public void setData(T data) {
        mData = data;
    }
}
