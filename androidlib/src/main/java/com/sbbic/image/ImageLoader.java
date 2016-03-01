package com.sbbic.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.sbbic.utils.BaseUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by God on 2016/2/19.
 * load image from net
 */
public class ImageLoader {

    private static final long DISK_CACHE_SIZE = 1024 * 1025 * 50;
    private static final int DISK_CACHE_INDEX = 0;
    private static final int BUFFER_SIZE = 1024 * 1024 * 2;
    private static final int TAG_KEY_URI = 1;
    private static final int MESSAGE_POST_RESULT = 1;
    private static final String TAG = ImageLoader.class.getSimpleName();
    private LruCache<String, Bitmap> mMemoryCache;
    private Context mContext;
    private DiskLruCache mDiskCache;
    private final ImageResizer mImageResizer;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_THREAD_COUNT = CPU_COUNT + 1;
    private static final int MAX_THREAD_COUNT = 2 * CPU_COUNT + 1;
    private static final long THREAD_KEEP_ALIVE = 10L;


    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger sum = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "ImageLoader#" + sum.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_THREAD_COUNT, MAX_THREAD_COUNT, THREAD_KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            LoaderResult obj = (LoaderResult) msg.obj;
            ImageView imageView = obj.imageView;
            imageView.setImageBitmap(obj.bitmap);


            //***************************解决滑动错位问题
            String uri = (String) imageView.getTag(TAG_KEY_URI);
            if (uri.equals(obj.url)) {
                imageView.setImageBitmap(obj.bitmap);
            } else {
                Log.w(TAG, "url has changed,ignored");
            }

        }
    };


    private ImageLoader(Context context) {
        mContext = context.getApplicationContext();
        initMemoryCache();
        initDiskCache();
        mImageResizer = new ImageResizer();
    }


    private void initMemoryCache() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 10024;
            }
        };

    }

    private void initDiskCache() {
        File diskCacheDir = BaseUtils.getDiskCacheDir(mContext, "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        try {
            mDiskCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ImageLoader build(Context context) {
        return new ImageLoader(context);
    }

    public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
        Bitmap bitmap = getFromMemory(uri);
        if (bitmap != null) {

            return bitmap;
        }

        try {
            bitmap = loadFromDiskCache(uri, reqWidth, reqHeight);
            if (bitmap != null) {
                return bitmap;
            }
            bitmap = loadFromHttp(uri, reqWidth, reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap == null) {
            bitmap = downLoad(uri);//同步加载无需缓存
        }
        return bitmap;
    }

    private Bitmap getFromMemory(String key) {
        return mMemoryCache.get(key);
    }

    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_KEY_URI, uri);
        Bitmap bitmap = getFromMemory(uri);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);

        }

        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bm = loadBitmap(uri, reqWidth, reqHeight);
                if (bm != null) {
                    //子线程更新UI
                    LoaderResult loaderResult = new LoaderResult(imageView, uri, bm);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, loaderResult).sendToTarget();

                }
            }
        };

        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);


    }


    private void putToMemeory(String key, Bitmap bitmap) {
        if (getFromMemory(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap loadFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not work on UI Thread");
        }
        if (mDiskCache == null) {
            return null;
        }
        String key = BaseUtils.getMD5(url);
        DiskLruCache.Editor editor = mDiskCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downLoad(url, outputStream)) {//将下载的图片保存到DiskCache中
                editor.commit();
            } else {
                editor.abort();
            }
        }
        mDiskCache.flush();
        return loadFromDiskCache(url, reqWidth, reqHeight);
    }

    private boolean downLoad(String urlString, OutputStream ous) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), BUFFER_SIZE);
            out = new BufferedOutputStream(ous, BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Bitmap downLoad(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    private Bitmap loadFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("can't use in UI Thread");
        }

        if (mDiskCache == null) {
            return null;
        }

        String key = BaseUtils.getMD5(url);
        DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
        Bitmap bitmap = null;
        if (snapshot != null) {
            FileInputStream fis = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            bitmap = mImageResizer.decode(fis.getFD(), reqWidth, reqHeight);
            if (bitmap != null) {
                putToMemeory(key, bitmap);//硬盘中存则则将该图片加载到MemoryCache
            }
        }
        return bitmap;
    }
}
