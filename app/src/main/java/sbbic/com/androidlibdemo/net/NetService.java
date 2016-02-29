package sbbic.com.androidlibdemo.net;

import com.alibaba.fastjson.JSON;
import com.sbbic.activity.BaseActivity;
import com.sbbic.net.DefaultThreadPool;
import com.sbbic.net.HttpRequest;
import com.sbbic.net.RequestCallback;
import com.sbbic.net.RequestParameter;
import com.sbbic.net.Response;
import com.sbbic.net.URLData;
import com.sbbic.net.URLManager;

import java.util.List;

import sbbic.com.androidlibdemo.mockdata.Mockdata;

/**
 * Created by God on 2016/2/29.
 */
public class NetService {

    private static NetService service=null;
    public NetService() {

    }

    public static synchronized NetService getInstance() {
        if (NetService.service == null) {
            service=new NetService();
        }
        return service;
    }




    public void invoke(final BaseActivity activity, final String apiKey, final List<RequestParameter> params,
                       final RequestCallback requestCallback, boolean forceUpdate) {


        URLData urlData = URLManager.findURL(activity, apiKey);

        if (urlData.getMockClass() != null) {//use mock
            try {
                Mockdata mockdata = (Mockdata) Class.forName(urlData.getMockClass()).newInstance();
                String strResponse = mockdata.getJsonData();
                Response response = JSON.parseObject(strResponse, Response.class);
                if (requestCallback != null) {
                    if (response.hasError()) {
                        requestCallback.onFail(response.getErrorMessage());

                    }else {
                        requestCallback.onSuccess(response.getResult());
                    }
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            if (forceUpdate) {
                urlData.setExpires(0L);
            }
            HttpRequest request = activity.getRequestManager().createRequest(urlData, params, requestCallback);
            DefaultThreadPool.getInstance().execute(request);
        }



    }
    public void invoke(final BaseActivity activity, final String apiKey, final List<RequestParameter> params,
                       final RequestCallback requestCallback) {

        this.invoke(activity,apiKey,params,requestCallback,false);
    }


}
