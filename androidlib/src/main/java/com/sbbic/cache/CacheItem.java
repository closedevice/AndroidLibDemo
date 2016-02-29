package com.sbbic.cache;

import java.io.Serializable;

/**
 * Created by God on 2016/2/29.
 */
public class CacheItem implements Serializable{

    private long timeStamp=0L;
    private String data;
    private String key;

    public CacheItem(String md5Key, String data, long expiredTime) {
        this.data=data;
        this.key=md5Key;
        this.timeStamp = System.currentTimeMillis() + expiredTime;

    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getData() {
        return data;
    }

    public String getKey() {
        return key;
    }
}
