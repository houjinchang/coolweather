package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 29208 on 2018/11/22.
 */
public class Basic {
    @SerializedName("city")
    public String cityName ;

    @SerializedName("id")//@SerializedName()注解的方式让json字段跟Java字段建立映射关系
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
