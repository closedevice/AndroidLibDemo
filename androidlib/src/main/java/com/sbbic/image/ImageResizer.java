package com.sbbic.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * Created by God on 2016/2/20.
 * you can adjust the picture size
 */
public class ImageResizer {
    private static final String TAG = ImageResizer.class.getSimpleName();

    public Bitmap decode(Resources res, int resid, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resid, options);
        int inSampleSize = calculateInSampleSize(reqWidth, reqHeight, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        Log.d(TAG, "inSampleSize:" + inSampleSize);
        return BitmapFactory.decodeResource(res, resid, options);
    }

    public Bitmap decode(FileDescriptor fd, int reqWidht, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        int inSampleSize = calculateInSampleSize(reqWidht, reqHeight, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }


    private int calculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options) {
        int oldHeight = options.outHeight;
        int oldWidth = options.outWidth;

        int inSampleSize = 1;

        if (oldHeight > reqHeight || oldWidth > reqWidth) {
            int halfHeight = oldHeight / 2;
            int halfWidth = oldWidth / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
