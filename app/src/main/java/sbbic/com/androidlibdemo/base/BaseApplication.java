package sbbic.com.androidlibdemo.base;

import android.app.Application;

import com.sbbic.cache.CacheManager;

/**
 * Created by God on 2016/2/29.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        CacheManager.getInstance().initCacheDir();
        super.onCreate();

    }
}
