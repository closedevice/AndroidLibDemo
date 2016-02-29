package sbbic.com.androidlibdemo.mockdata;

import com.sbbic.net.Response;

/**
 * Created by God on 2016/2/29.
 */
public abstract class Mockdata {
    public abstract String getJsonData();

    public Response getSuccessResponse() {
        Response response = new Response();
        response.setError(false);
        response.setErrorType(0);
        response.setErrorMessage("");

        return response;
    }

    public Response getFailResponse(int errorType,String errorMessage) {
        Response response = new Response();
        response.setError(true);
        response.setErrorType(errorType);
        response.setErrorMessage(errorMessage);

        return response;
    }
}
