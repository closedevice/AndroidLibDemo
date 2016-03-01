package com.sbbic.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.sbbic.net.cache.CacheManager;
import com.sbbic.utils.BaseUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by God on 2016/2/29.
 * http request,just support get,post
 */
public class HttpRequest implements Runnable {
    private String TAG = HttpRequest.class.getSimpleName();


    private static final String REQUEST_GET = "get";
    private static final String POST = "post";
    private HttpUriRequest request = null;
    private URLData urlData = null;
    private RequestCallback requestCallback = null;
    private List<RequestParameter> parameters;
    private String url = null;
    private DefaultHttpClient httpClient = null;
    private HttpResponse response;
    private Handler handler;
    private String newUrl;


    public HttpRequest(final URLData data, List<RequestParameter> parameters, RequestCallback requestCallback) {
        urlData = data;
        url = urlData.getUrl();
        this.parameters = parameters;
        this.requestCallback = requestCallback;

        handler = new Handler();
        System.out.println(handler.getLooper().getThread().getName());
        System.out.println(new Handler(Looper.getMainLooper()).getLooper().getThread().getName());

        if (httpClient == null) {
            httpClient = new DefaultHttpClient();

        }


    }

    public HttpUriRequest getRequest() {
        return request;
    }


    @Override

    public void run() {

        try {

            switch (urlData.getNetType()) {
                case REQUEST_GET:
                    StringBuffer paramBuffer = new StringBuffer();
                    if ((parameters != null) && (parameters.size() > 0)) {
                        sortKeys();

                        for (RequestParameter parameter : parameters) {
                            if (paramBuffer.length() == 0) {
                                paramBuffer.append(parameter.getName() + "=" + BaseUtils.UrlEncodeUnicode(parameter.getValue()));
                            } else {
                                paramBuffer.append("&" + parameter.getName() + "=" + BaseUtils.UrlEncodeUnicode(parameter.getValue()));
                            }
                        }

                        newUrl = url + "?" + paramBuffer.toString();
                    } else {
                        newUrl = url;
                    }

                    if (urlData.getExpires() > 0) {
                        final String content = CacheManager.getInstance().getCache(newUrl);
                        Log.d(TAG, "data from cache");
                        if (content != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    requestCallback.onSuccess(content);

                                }
                            });
                            return;
                        }
                    }

                    request = new HttpGet(newUrl);

                    break;
                case POST:
                    if (parameters != null && parameters.size() > 0) {
                        ArrayList<BasicNameValuePair> list = new ArrayList<>();
                        for (RequestParameter p : parameters) {
                            list.add(new BasicNameValuePair(p.getName(), p.getValue()));

                        }
                        ((HttpPost) request).setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
                    }
                    break;
                default:
                    return;
            }

            request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
            request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);

            //发送请求
            response = httpClient.execute(request);

            //请求状态
            int statusCode = response.getStatusLine().getStatusCode();


            //未设置回掉表示不需要知道返回结果，则无需解析返回
            if (requestCallback != null) {
                if (statusCode == HttpStatus.SC_OK) {
                    ByteArrayOutputStream content = new ByteArrayOutputStream();

                    response.getEntity().writeTo(content);
                    final String strREsponse = new String(content.toByteArray()).trim();
                    if (requestCallback != null) {
                        final Response responseInJson = JSON.parseObject(strREsponse, Response.class);
                        if (responseInJson.hasError()) {
                            handleNetworkError(responseInJson.getErrorMessage());
                        } else {
                            //缓存Get请求的数据
                            if (urlData.getNetType().equals(REQUEST_GET) && urlData.getExpires() > 0) {
                                Log.d(TAG, "store data to cache");
                                CacheManager.getInstance().putCache(newUrl, responseInJson.getResult(),
                                        urlData.getExpires());
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    HttpRequest.this.requestCallback.onSuccess(responseInJson.getResult());

                                }
                            });
                        }

                    } else {
                        handleNetworkError("net error");
                    }


                } else {
                    handleNetworkError("net error");
                }
            }

        } catch (UnsupportedEncodingException e) {
            handleNetworkError("net error");
        } catch (ClientProtocolException e) {
            handleNetworkError("net error");
        } catch (IOException e) {
            handleNetworkError("net error");
        }
    }

    void sortKeys() {
        for (int i = 1; i < parameters.size(); i++) {
            for (int j = i; j > 0; j--) {
                RequestParameter p1 = parameters.get(j - 1);
                RequestParameter p2 = parameters.get(j);
                if (compare(p1.getName(), p2.getName())) {
                    String name = p1.getName();
                    String value = p1.getValue();

                    p1.setName(p2.getName());
                    p1.setValue(p2.getValue());

                    p2.setName(name);
                    p2.setValue(value);
                }
            }
        }
    }

    boolean compare(String str1, String str2) {
        String uppStr1 = str1.toUpperCase();
        String uppStr2 = str2.toUpperCase();

        boolean str1IsLonger = true;
        int minLen = 0;

        if (str1.length() < str2.length()) {
            minLen = str1.length();
            str1IsLonger = false;
        } else {
            minLen = str2.length();
            str1IsLonger = true;
        }

        for (int index = 0; index < minLen; index++) {
            char ch1 = uppStr1.charAt(index);
            char ch2 = uppStr2.charAt(index);
            if (ch1 != ch2) {
                if (ch1 > ch2) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return str1IsLonger;
    }

    private void handleNetworkError(final String errorMessage) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                HttpRequest.this.requestCallback.onFail(errorMessage);

            }
        });
    }
}
