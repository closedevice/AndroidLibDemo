package sbbic.com.androidlibdemo;

import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sbbic.net.RequestCallback;
import com.sbbic.net.RequestParameter;

import java.util.ArrayList;

import sbbic.com.androidlibdemo.base.AppBaseActivity;
import sbbic.com.androidlibdemo.entry.WeatherInfo;
import sbbic.com.androidlibdemo.net.NetService;

/**
 * Created by God on 2016/2/29.
 */
public class WeatherActivity extends AppBaseActivity {

    private RequestCallback weatherCallback;
    private TextView tvCity;
    private TextView tvCityId;

    @Override
    protected void initVar() {

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_weather);
        tvCity = (TextView) findViewById(R.id.tvCity);
        tvCityId = (TextView) findViewById(R.id.tvCityId);

    }

    @Override
    protected void loadData() {
        weatherCallback=new RequestCallback() {
            @Override
            public void onSuccess(String content) {
                WeatherInfo weatherInfo = JSON.parseObject(content, WeatherInfo.class);
                if (weatherInfo != null) {
                    tvCity.setText(weatherInfo.getCity());
                    tvCityId.setText(weatherInfo.getCityid());
                }
            }

            @Override
            public void onFail(String errorMessage) {

            }
        };

        ArrayList<RequestParameter> params = new ArrayList<RequestParameter>();
        RequestParameter rp1 = new RequestParameter("cityId", "111");
        RequestParameter rp2 = new RequestParameter("cityName", "Beijing");
        params.add(rp1);
        params.add(rp2);

        NetService.getInstance().invoke(this, "getWeatherInfo", params, weatherCallback);
    }

}
