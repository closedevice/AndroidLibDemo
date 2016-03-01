package sbbic.com.androidlibdemo.base;

import android.app.Application;

import com.sbbic.net.cache.CacheManager;

/**
 * Created by God on 2016/2/29.
 *
 * base application,you can extends it
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        CacheManager.getInstance().initCacheDir();
        super.onCreate();

    }
}
