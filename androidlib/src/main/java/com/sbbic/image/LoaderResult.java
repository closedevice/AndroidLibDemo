package com.sbbic.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by God on 2016/2/20.
 * <p/>
 * load result
 */
public class LoaderResult {
    public ImageView imageView;
    public String url;
    public Bitmap bitmap;

    public LoaderResult(ImageView imageView, String url, Bitmap bitmap) {
        this.imageView = imageView;
        this.url = url;
        this.bitmap = bitmap;
    }
}
