package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 29208 on 2018/11/22.
 */
public class Forecast {
    public String date;

    @SerializedName("cond")
    public More more;

    @SerializedName("tmp")
    public  Tempeture tempeture;


    public class Tempeture{
        public String max;
        public  String min;
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
