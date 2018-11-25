package com.example.coolweather;

import android.content.SharedPreferences;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;



import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;
    public String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化各种控件
        weatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        weatherInfoText= (TextView) findViewById(R.id.weather_info_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText= (TextView) findViewById(R.id.aqi_text);
        pm25Text= (TextView) findViewById(R.id.pm25_text);
        comfortText= (TextView) findViewById(R.id.comfort_text);
        carWashText= (TextView) findViewById(R.id.car_wash_text);
        sportText= (TextView) findViewById(R.id.sport_text);
        bingPicImg= (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton= (Button) findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //读取天气数据
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);

        if(weatherString!=null){
            //有缓存时候直接解析天气数据

            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId; Log.d("mweatherId","有缓存"+mWeatherId);
            showWeatherInfo(weather);
        }
        else{
            //无缓存时候去服务器查询数据
            mWeatherId=getIntent().getStringExtra("weather_id");
            Log.d("mweatherId","无缓存"+mWeatherId);
            //把天气主界面设置不可见
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
            Log.d("png","yes");
        }
        else{
            loadBingPic();
        }



    }

    private void loadBingPic(){
        String requestBIngPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBIngPic, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                final  String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }


    //根据天气id请求城市天气信息
    public  void requestWeather(final String weatherId){
        //获取weatherUrl(Stirng)，，自己根据weatherId构造一个网址（adress）
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=b93a2769f4d341b084a190022f15c5e7";
        //调用HttpUtil的请求数据方法(sendOkHttpRequest)...需要传入网址，，，，活得回调
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            //若没有response
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                //runOnUiThread回到主线程才能修改ui
                runOnUiThread(new Runnable() {
                    @Override//实现Runnable接口的run方法
                    public void run() {
                        //土司失败
                       Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        //取消刷新
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            //有response，，，，传回response
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                //新建字符串，，利用responseText.body().string()方法获得字符串存储在responseText中
                final String responseText=response.body().string();
                Log.d("WeatherActivity","onResponse"+responseText);
                //根据Utility 的handleWeatherResponse方法获得weather对象，需要向该方法传入json数据
                final Weather weather=Utility.handleWeatherResponse(responseText);
                //修改ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果传过来的weatherId不是空字符串，而且weather对象的status为ok,就把responseText存进sp(存储键值对)，也就是response.body.string()
                        if(weatherId!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);//键值对存储
                            editor.apply();//提交存储
                            mWeatherId=weather.basic.weatherId;
//                            Log.d("mweatherId","获取之后"+mWeatherId);
                            //调用show方法设置ui
                            showWeatherInfo(weather);

                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        //关闭刷新
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }


    private void showWeatherInfo(Weather weather){
//        根据weather对象获取一堆字符串
        String cityName=weather.basic.cityName;//所属城市名
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";//温度
        String weatherInfo=weather.now.more.info;//多云，，，晴天。。。。
        titleCity.setText(cityName);



        Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
        t.setToNow(); // 取得系统时间。
        int year = t.year;
        int month = t.month+1;
        int day = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        titleUpdateTime.setText(hour+":"+minute);








//        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        //ViewGroup的removeAllView()方法，清楚所有插入的layout
        forecastLayout .removeAllViews();
//        Log.d("1",weather.forecastList.get(1).more.info.toString())
// 遍历所有预报
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=view.findViewById(R.id.date_text);
            TextView infoText=view.findViewById(R.id.info_text);
            TextView maxText=view.findViewById(R.id.max_text);
            TextView minText=view.findViewById(R.id.min_text);
//            Log.d("info",forecast.toString()+forecast.date+" "+forecast.more.info+" "+forecast.tempeture.max+" "+forecast.tempeture.min);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.tempeture.max+"℃");
            minText.setText(forecast.tempeture.min+"℃");
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort ="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        //把天气主界面设置为可见
        weatherLayout.setVisibility(View.VISIBLE);





    }





}
