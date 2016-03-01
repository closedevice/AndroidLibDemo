package com.sbbic.net.cache;

import android.os.Environment;

import com.sbbic.utils.BaseUtils;

import java.io.File;

/**
 * Created by God on 2016/2/29.
 *
 * net cache manager,just for get-request
 */
public class CacheManager {
    public static final String APP_CACHE_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/androidcache/appdata/";
    public static final long SCCARD_MIN_SPACE=1020*1024*10;

    private static CacheManager cacheManager;

    public static synchronized CacheManager getInstance() {
        if (cacheManager == null) {
            synchronized (CacheManager.class) {
                if (cacheManager == null) {
                    cacheManager=new CacheManager();
                }
            }
        }
        return cacheManager;
    }


    /**
     * you must call it before use CacheManager.
     */
    public void initCacheDir() {
        if (BaseUtils.sdcardMounted()) {
            if (BaseUtils.getSDSize() < SCCARD_MIN_SPACE) {
                clearAllData();
            }else{
                File dir = new File(APP_CACHE_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
        }
    }

    public String getCache(final String key) {
        String md5Key = BaseUtils.getMD5(key);
        if (isExist(md5Key)) {
            CacheObject item = getFromCache(md5Key);
            if (item != null) {
                return item.getData();
            }
        }
        return null;
    }

    public void putCache(final String key, final String data, long expiredTime) {
        String md5Key = BaseUtils.getMD5(key);
        CacheObject item = new CacheObject(md5Key, data, expiredTime);
        if (BaseUtils.getSDSize() > SCCARD_MIN_SPACE) {
            BaseUtils.saveObject(APP_CACHE_PATH+item.getKey()
                    ,item);
        }
    }


    private CacheObject getFromCache(String md5Key) {
        CacheObject cacheItem = null;
        Object o = BaseUtils.restoreObject(APP_CACHE_PATH + md5Key);
        if (o != null) {
            cacheItem = (CacheObject) o;
        }
        if (cacheItem == null) {
            return null;
        }

        if (System.currentTimeMillis()>cacheItem.getTimeStamp()) {
            return null;
        }


        return cacheItem;
    }

    private boolean isExist(String md5Key) {
        final File file=new File(APP_CACHE_PATH + md5Key);
        return file.exists();
    }
    public void clearAllData() {
        File file;
        File[] files;
        if (BaseUtils.sdcardMounted()) {
            file = new File(APP_CACHE_PATH);
            files = file.listFiles();
            if (files != null && files.length > 0) {
                for (final File f : files) {
                    f.delete();
                }
            }
        }
    }
}
