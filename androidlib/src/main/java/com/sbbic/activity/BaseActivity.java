package com.sbbic.activity;

import android.app.Activity;
import android.os.Bundle;

import com.sbbic.net.RequestManager;

/**
 * Created by God on 2016/2/29.
 */
public abstract class BaseActivity extends Activity {
    private String TAG=BaseActivity.class.getSimpleName();
    protected RequestManager requestManager=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestManager = new RequestManager(this);
        super.onCreate(savedInstanceState);
        initVar();
        initViews(savedInstanceState);
        loadData();
    }

    /**
     * init base variate
     */
    protected abstract void initVar() ;


    /**
     * init view
     * @param savedInstanceState
     */
    protected abstract void initViews(Bundle savedInstanceState);

    /**
     * you can load data ,such as a network request
     */
    protected abstract void loadData();

    @Override
    protected void onPause() {
        if (requestManager != null) {
            requestManager.cancelRequest();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (requestManager != null) {
            requestManager.cancelRequest();
        }
        super.onDestroy();
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }
}
