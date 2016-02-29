package com.sbbic.net;

import com.sbbic.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by God on 2016/2/29.
 */
public class RequestManager {

    ArrayList<HttpRequest> requestList=null;

    public RequestManager(final BaseActivity activity) {
        requestList = new ArrayList<>();
    }

    public void addRequest(final HttpRequest request) {
        requestList.add(request);
    }

    /**
     * cancle all request
     */
    public void cancelRequest() {
        if (requestList != null && requestList.size() > 0) {
            for (HttpRequest request : requestList) {
                try {
                    request.getRequest().abort();
                    requestList.remove(request.getRequest());
                } catch (Exception e) {

                }
            }
        }
    }


    public HttpRequest createRequest(final URLData urlData, final RequestCallback requestCallback) {
        return createRequest(urlData, null, requestCallback);
    }

    /**
     * create request
     * @param urlData
     * @param requestCallback
     * @return
     */
    public HttpRequest createRequest(final URLData urlData, final List<RequestParameter> params, final
    RequestCallback requestCallback) {
        final HttpRequest request = new HttpRequest(urlData, params, requestCallback);
        addRequest(request);
        return request;
    }
}
