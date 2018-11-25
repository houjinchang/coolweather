package com.example.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

/**
 * 工具类，，，处理简单是的json数据，，真正复杂的json数据要使用Gson来解析
 * 三个方法分别处理省级市级县级json数据，，，都是先使用JSONArray和JSONObject先把数据解析出来，然后组装成实体类对象，再调用save方法存储
 *
 * Created by 29208 on 2018/11/19.
 */
public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
//        Log.d("exe","handleProvinceResponse");
//        Log.d("Utility","handleProvinceResponse");
        if (!TextUtils.isEmpty(response)) {
            try {
                //JSONArray用来处理json数据
                JSONArray allProvinces = new JSONArray(response);

                for (int i = 0; i < allProvinces.length(); i++) {
                    //JSOnArray逐个读取JSONObject,,,,,,,,,,,,getJsonObject(index)
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    //创建Province并存储（继承自DataSupport的类）
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
//                    调用save方法存储到数据库中
                    province.save();

//                    Log.d("houjinchang", province.getProvinceName() + " " + province.getProvinceCode() + " id=" + provinceObject.getInt("id")+" result"+x);

                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据          传入response和provinceId
     */
    public static boolean handleCityResponse(String response, int provinceId) {
//        Log.d("exe","handleCityResponse");
//        Log.d("Utility","handleCityResponse");
//        LitePal.getDatabase();
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                //创建 所有的City对象并且存储
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据              传入response和cityId
     */
    public static boolean handleCountyResponse(String response, int cityId) {
//        Log.d("exe","handleCountyResponse");
//        Log.d("Utility", "handleCountyResponse");
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }




    //将传入的json数据解析成weather实体类,,,,根据JSONObject构建JSONArray
    public static Weather handleWeatherResponse(String response){
        try{
            Log.d("info","Utility"+response);
            //根据构造器构建出JSONObject对象
//            JSONObject jsonObject=JSONOb
            JSONObject jsonObject=new JSONObject(response);
            //构造已获取“heweather”的jsonArray
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
//            创建天气内容字符串
            String weatherContent=jsonArray.getJSONObject(0).toString();
//            返回weather对象
            Log.d("infox","Utility2"+jsonArray.getJSONObject(0).toString());
            Log.d("infox","Utility2"+jsonArray.getJSONObject(0).toString());
            Weather weather=new Gson().fromJson(weatherContent,Weather.class);
//            Log.d("info",weather.forecastList.get(1).toString()+weather.forecastList.get(1).date+" "+weather.forecastList.get(1).more.info+" "+weather.forecastList.get(1).tempeture.max+" "+weather.forecastList.get(1).tempeture.min);
            return weather;

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
