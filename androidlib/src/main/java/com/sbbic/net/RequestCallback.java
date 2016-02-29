package com.sbbic.net;

/**
 * Created by God on 2016/2/29.
 */
public interface RequestCallback {
    void onSuccess(String content);

    void onFail(String errorMessage);
}
