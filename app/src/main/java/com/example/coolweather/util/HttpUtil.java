package com.example.coolweather.util;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 发送网络请求
 * Created by 29208 on 2018/11/19.
 */


public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        Log.d("exe","HttpUtil.sendOkHttpRequest");
        Log.d("HttpUtil","sendOkHttpResquest");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
