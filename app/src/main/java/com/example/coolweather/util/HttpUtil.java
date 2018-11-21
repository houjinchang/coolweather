package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 发送网络请求
 * Created by 29208 on 2018/11/19.
 */
public class HttpUtil  {
    //发起http请求，传入网址，并且注册一个回调来处理服务响应
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
