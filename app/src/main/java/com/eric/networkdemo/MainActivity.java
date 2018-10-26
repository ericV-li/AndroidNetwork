package com.eric.networkdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.eric.core.core.HttpClientManager;
import com.eric.core.listener.WebCallbackListener;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
/**
 * @author li
 * @Package com.eric.networkdemo
 * @Title: MainActivity
 * @Description: Copyright (c)
 * Create DateTime: 2018/10/26
 */
public class MainActivity extends AppCompatActivity {
    private final String URL = "http://10.7.30.49:8080/xxxx/d/login";//接口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Map<String, String> params = new HashMap<>();//入参
        params.put("mobile","xxxx");
        params.put("code","xxxx");
        HttpClientManager.getInstance().asyncPost(URL, 12345, null, null, params, new WebCallbackListener<String>() {
            @Override
            public void onSuccess(Object i, int i1, final String result, Headers headers) {
                Log.i("result", result);
            }

            @Override
            public void onData(Object i, int i1, String s) {

            }

            @Override
            public void onFailure(Object i, Exception e) {

            }
        }, false);
    }

}
