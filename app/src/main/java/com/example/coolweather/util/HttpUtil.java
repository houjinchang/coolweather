package com.example.coolweather.util;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 发送网络请求
 * Created by 29208 on 2018/11/19.
 */


public class HttpUtil {
    //与服务器交互
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
//        Log.d("exe","HttpUtil.sendOkHttpRequest");
//        Log.d("HttpUtil","sendOkHttpResquest");
//        新建OkHttpClient对象
        OkHttpClient client = new OkHttpClient();
        //建立request对象用来发送http请求
        Request request = new Request.Builder()
                .url(address)
                .build();
//        发送请求
//        Response response=client.newCall(request).execute();


        //发送request并且设置回调
        client.newCall(request).enqueue(callback);
    }

}
