package sbbic.com.androidlibdemo.mockdata;

import com.alibaba.fastjson.JSON;
import com.sbbic.net.Response;

import sbbic.com.androidlibdemo.entry.WeatherInfo;

/**
 * Created by God on 2016/2/29.
 */
public class MockWeatherInfo extends Mockdata {
    @Override
    public String getJsonData() {
        WeatherInfo weather = new WeatherInfo();
        weather.setCity("Beijing");
        weather.setCityid("10000");

        Response response = getSuccessResponse();
        response.setResult(JSON.toJSONString(weather));
        return JSON.toJSONString(response);
    }
}
